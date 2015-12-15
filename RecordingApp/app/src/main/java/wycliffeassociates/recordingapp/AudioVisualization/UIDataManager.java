package wycliffeassociates.recordingapp.AudioVisualization;

import android.app.Activity;
import android.util.Pair;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.TextView;

import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import wycliffeassociates.recordingapp.AudioInfo;
import wycliffeassociates.recordingapp.Playback.WavPlayer;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.Recording.RecordingMessage;
import wycliffeassociates.recordingapp.Recording.RecordingQueues;
import wycliffeassociates.recordingapp.Recording.WavFileWriter;
import wycliffeassociates.recordingapp.WavFileLoader;

/**
 * Created by sarabiaj on 9/10/2015.
 */

public class UIDataManager {

    static public final boolean PLAYBACK_MODE = true;
    static public final boolean RECORDING_MODE = false;
    private final WaveformView mainWave;
    private final MinimapView minimap;
    private final Activity ctx;
    private boolean isRecording;
    public static Semaphore lock;
    private WavFileLoader wavLoader;
    private WavVisualizer wavVis;
    private MappedByteBuffer buffer;
    private MappedByteBuffer mappedAudioFile;
    private MappedByteBuffer preprocessedBuffer;
    private int secondsOnScreen = 5;
    private Animation anim;
    RecordingTimer timer;
    private final TextView timerView;
    private boolean mode;
    private boolean isALoadedFile = false;


    public UIDataManager(WaveformView mainWave, MinimapView minimap, Activity ctx, boolean mode, boolean isALoadedFile){
        this.isALoadedFile = isALoadedFile;
        this.mode = mode;
        mainWave.setUIDataManager(this);
        minimap.setUIDataManager(this);
        this.mainWave = mainWave;
        this.minimap = minimap;
        timerView = (TextView)ctx.findViewById(R.id.timerView);
        this.ctx = ctx;
        lock = new Semaphore(1);

        anim = new RotateAnimation(0f, 350f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(1500);
        timer = new RecordingTimer();
    }

    public void startTimer(){
        timer.startTimer();
    }
    public void pauseTimer(){
        timer.pause();
    }
    public void resumeTimer(){
        timer.resume();
    }

    public MappedByteBuffer getMappedAudioFile(){
        return mappedAudioFile;
    }

    public void setIsRecording(boolean isRecording){
        this.isRecording = isRecording;
    }

    public void updateUI(){
        if(minimap == null || mainWave == null || WavPlayer.getDuration() == 0){
            return;
        }
        if(wavLoader.visFileLoaded()){
            wavVis.enableCompressedFileNextDraw(wavLoader.getMappedCacheFile());
        }
        //Marker is set to the percentage of playback times the width of the minimap
        int location = WavPlayer.getLocation();
        minimap.setMiniMarkerLoc((float) ((location / (double) WavPlayer.getDuration()) * minimap.getWidth()));
        drawWaveformDuringPlayback(location);
        mainWave.setTimeToDraw(location);
        final String time = String.format("%02d:%02d:%02d", location / 3600000, (location / 60000) % 60, (location / 1000) % 60);
        ctx.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                timerView.setText(time);
                timerView.invalidate();
            }
        });

    }

    public void enablePlay(){
        if(mode == PLAYBACK_MODE) {
            ctx.findViewById(R.id.btnPause).setVisibility(View.INVISIBLE);
            ctx.findViewById(R.id.btnPlay).setVisibility(View.VISIBLE);
        }
    }

    public void swapPauseAndPlay(){
        if(ctx.findViewById(R.id.btnPause).getVisibility() == View.VISIBLE) {
            ctx.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ctx.findViewById(R.id.btnPause).setVisibility(View.INVISIBLE);
                    ctx.findViewById(R.id.btnPlay).setVisibility(View.VISIBLE);
                }
            });
        }
        else{
            ctx.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ctx.findViewById(R.id.btnPlay).setVisibility(View.INVISIBLE);
                    ctx.findViewById(R.id.btnPause).setVisibility(View.VISIBLE);
                }
            });
        }
    }

    public void swapPauseAndRecord(){
        if(ctx.findViewById(R.id.btnRecording).getVisibility() == View.INVISIBLE) {
            ctx.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ctx.findViewById(R.id.btnRecording).setVisibility(View.VISIBLE);
                    ctx.findViewById(R.id.btnPauseRecording).setVisibility(View.INVISIBLE);
                    ctx.findViewById(R.id.btnPauseRecording).setAnimation(null);
                }
            });
        }
        else{
            ctx.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ctx.findViewById(R.id.btnPauseRecording).setVisibility(View.VISIBLE);
                    ctx.findViewById(R.id.btnRecording).setVisibility(View.INVISIBLE);
                    ctx.findViewById(R.id.btnPauseRecording).setAnimation(anim);
                }
            });
        }
    }

    public void cutAndUpdate(){
        int start = CanvasView.getStartMarker();
        int end = CanvasView.getEndMarker();
        System.out.println("got the markers");
        wavLoader = wavLoader.cut(start, end);
        buffer = null;
        preprocessedBuffer = null;
        mappedAudioFile = null;
        wavVis = null;
        System.out.println("Should have created a new wav Loader");
        buffer = wavLoader.getMappedFile();
        preprocessedBuffer = wavLoader.getMappedCacheFile();
        mappedAudioFile = wavLoader.getMappedAudioFile();
        minimap.init(wavLoader.getMinimap(minimap.getWidth(), minimap.getHeight()));
        wavVis = new WavVisualizer(buffer, null, mainWave.getWidth(), mainWave.getHeight());
        //WavPlayer.loadFile(mappedAudioFile);
        CanvasView.clearMarkers();
        updateUI();
    }

    public void loadWavFromFile(String path){

        wavLoader = new WavFileLoader(path, mainWave.getWidth(), isALoadedFile);
        buffer = wavLoader.getMappedFile();
        preprocessedBuffer = wavLoader.getMappedCacheFile();
        mappedAudioFile = wavLoader.getMappedAudioFile();
        System.out.println("Mapped files completed.");
//      System.out.println("Compressed file is size: " + preprocessedBuffer.capacity() + " Regular file is size: " + buffer.capacity() + " increment is " + (int)Math.floor((AudioInfo.SAMPLERATE * 5)/mainWave.getWidth()));
        minimap.init(wavLoader.getMinimap(minimap.getWidth(), minimap.getHeight()));
        WavPlayer.loadFile(getMappedAudioFile());
        minimap.setAudioLength(WavPlayer.getDuration());
        System.out.println("There was an error with mapping the files");
        wavVis = new WavVisualizer(buffer, preprocessedBuffer, mainWave.getWidth(), mainWave.getHeight());
    }
    //NOTE: Only one instance of canvas view can call this; otherwise two threads will be pulling from the same queue!!
    public void listenForRecording(boolean drawWaveform){
        mainWave.setDrawingFromBuffer(true);
        ctx.findViewById(R.id.volumeBar).setVisibility(View.VISIBLE);
        ctx.findViewById(R.id.volumeBar).setBackgroundResource(R.drawable.min);
        Thread uiThread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isStopped = false;
                boolean isPaused = false;
                double maxDB = 0;
                long timeDelay = System.currentTimeMillis();
                while (!isStopped) {
                    try {
                        RecordingMessage message = RecordingQueues.UIQueue.take();
                        isStopped = message.isStopped();
                        isPaused = message.isPaused();

                        if (!isPaused && message.getData() != null) {
                            lock.acquire();
                            byte[] buffer = message.getData();
                            lock.release();

                            double max = getPeakVolume(buffer);
                            double db = computeDB(max);
                            if(db > maxDB && ((System.currentTimeMillis() - timeDelay) < 500)){
                                VolumeMeter.changeVolumeBar(ctx, db);
                                maxDB = db;
                            }
                            else if(((System.currentTimeMillis() - timeDelay) > 500)){
                                VolumeMeter.changeVolumeBar(ctx, db);
                                maxDB = db;
                                timeDelay = System.currentTimeMillis();
                            }

                            if(isRecording) {
                                lock.acquire();
                                mainWave.setBuffer(buffer);
                                lock.release();
                                mainWave.postInvalidate();
                                if(timer != null) {
                                    long t = timer.getTimeElapsed();
                                    final String time = String.format("%02d:%02d:%02d", t / 3600000, (t / 60000) % 60, (t / 1000) % 60);
                                    ctx.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            timerView.setText(time);
                                            timerView.invalidate();
                                        }
                                    });
                                }
                            }
                            //changeVolumeBar(ctx, db);
                        }
                        if (isStopped) {
                            lock.acquire();
                            mainWave.setBuffer(null);
                            lock.release();
                            mainWave.postInvalidate();
                            return;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                mainWave.setDrawingFromBuffer(false);
            }
        });
        uiThread.start();
    }

    public double getPeakVolume(byte[] buffer){
        double max = 0;
        for(int i =0; i < buffer.length; i+=2) {
            byte low = buffer[i];
            byte hi = buffer[i + 1];
            short value = (short)(((hi << 8) & 0x0000FF00) | (low & 0x000000FF));
            max = (Math.abs(value) > max)? Math.abs(value) : max;
        }
        return max;
    }

    public double computeDB(double value){
        return  Math.log10(value / (double) AudioInfo.AMPLITUDE_RANGE)* 20;
    }

    public void drawWaveformDuringPlayback(int location){
        mainWave.setDrawingFromBuffer(false);
        long startTime = System.nanoTime();
        try {
            lock.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mainWave.setMarkerToDrawStart(CanvasView.getMarkerStartTime());
        mainWave.setMarkerToDrawEnd(CanvasView.getMarkerEndTime());
        float[] samples = wavVis.getDataToDraw(location, WavFileWriter.largest);
        lock.release();
        mainWave.setIsDoneDrawing(false);
        //System.out.println("Time taken to generate samples to draw: " + (System.nanoTime()-startTime));
        //System.out.println("Size of buffer to draw is "+samples.size());
        try {
            lock.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mainWave.setWaveformDataForPlayback(samples);
        lock.release();
        mainWave.postInvalidate();
        Runtime.getRuntime().freeMemory();
    }

    private float[] convertToArray(ArrayList<Pair<Double,Double>> samples){
        float[] path = new float[samples.size()*4];
        for(int i = 0; i < samples.size(); i++){
            path[i*4] = i;
            path[i*4+1] = (float)(samples.get(i).first + mainWave.getHeight() / 2);
            path[i*4+2] = i;
            path[i*4+3] = (float)(samples.get(i).second + mainWave.getHeight() / 2);
        }
        return path;
    }
}
