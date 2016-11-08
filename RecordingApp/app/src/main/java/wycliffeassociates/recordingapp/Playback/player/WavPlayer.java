package wycliffeassociates.recordingapp.Playback.player;

import java.nio.ShortBuffer;
import java.util.List;

import wycliffeassociates.recordingapp.Playback.Editing.CutOp;
import wycliffeassociates.recordingapp.wav.WavCue;

/**
 * Created by sarabiaj on 10/28/2016.
 */

/**
 * Controls interaction between BufferPlayer and AudioBufferProvider. The BufferPlayer simply plays audio
 * that is passed to it, and the BufferProvider manages processing audio to get the proper buffer to onPlay
 * based on performing operations on the audio buffer (such as cut).
 */
public class WavPlayer {

    private final List<WavCue> mCueList;
    ShortBuffer mAudioBuffer;
    CutOp mOperationStack;
    BufferPlayer mPlayer;
    AudioBufferProvider mBufferProvider;
    WavPlayer.OnCompleteListener mOnCompleteListener;
    private int EPSILON = 200;

    public interface OnCompleteListener{
        void onComplete();
    }

    public WavPlayer(ShortBuffer audioBuffer, CutOp operations, List<WavCue> cueList){
        mOperationStack = operations;
        mAudioBuffer = audioBuffer;
        mBufferProvider = new AudioBufferProvider(mAudioBuffer, mOperationStack);
        mCueList = cueList;
        mPlayer = new BufferPlayer(mBufferProvider, new BufferPlayer.OnCompleteListener() {
            @Override
            public void onComplete() {
                if(mOnCompleteListener != null){
                    mBufferProvider.reset();
                    mOnCompleteListener.onComplete();
                }
            }
        });
    }

    public synchronized void seekNext(){
        int seekLocation = getDurationInFrames();
        int currentLocation = getLocationInFrames();
        if(mCueList != null) {
            int location;
            for(int i = 0; i < mCueList.size(); i++){
                location = mCueList.get(i).getLocation();
                if(currentLocation < location){
                    seekLocation = location;
                    break;
                }
            }
        }
        seekTo(Math.min(seekLocation, mBufferProvider.getLimit()));
    }

    public synchronized void seekPrevious(){
        int seekLocation = 0;
        int currentLocation = getLocationInFrames();
        if(mCueList != null){
            int location;
            for(int i = mCueList.size()-1; i >= 0; i--){
                location = mCueList.get(i).getLocation();
                //if playing, you won't be able to keep pressing back, it will clamp to the last marker
                if(!isPlaying() && currentLocation > location) {
                    seekLocation = location;
                    break;
                } else if (currentLocation - EPSILON > location) { //epsilon here is to prevent that clamping
                    seekLocation = location;
                    break;
                }
            }
        }
        seekTo(Math.max(seekLocation, mBufferProvider.getMark()));
    }

    private synchronized void seekTo(int absoluteFrame){
        if(absoluteFrame > getDurationInFrames() || absoluteFrame < 0){
            return;
        }
        boolean wasPlaying = mPlayer.isPlaying();
        pause();
        mBufferProvider.setPosition(absoluteFrame);
        if(wasPlaying){
            play();
        }
    }

    public void play(){
        mPlayer.play(mBufferProvider.getSizeOfNextSession());
    }

    public void pause(){
        mPlayer.pause();
    }

    public void setOnCompleteListener(WavPlayer.OnCompleteListener onCompleteListener){
        mOnCompleteListener = onCompleteListener;
    }

    public int getLocationMs(){
        return (int)(getLocationInFrames() / 44.1);
    }

    public int getLocationInFrames(){
        int relativeLocationOfHead = mOperationStack.absoluteLocToRelative(mBufferProvider.getStartPosition(), false) + mPlayer.getPlaybackHeadPosition();
        int absoluteLocationOfHead = mOperationStack.relativeLocToAbsolute(relativeLocationOfHead, false);
        return absoluteLocationOfHead;
    }

    public int getDuration(){
        return (int)(mBufferProvider.getDuration() / 44.1);
    }

    public int getDurationInFrames(){
        return mBufferProvider.getDuration();
    }

    public boolean isPlaying(){
        return mPlayer.isPlaying();
    }

    public void setLoopStart(int frame){
        mBufferProvider.mark(frame);
    }

    public int getLoopStart(){
        return (int)(mBufferProvider.getMark() / 44.1);
    }

    public void setLoopEnd(int frame){
        mBufferProvider.setLimit(frame);
        mBufferProvider.reset();
    }

    public int getLoopEnd(){
        return (int)(mBufferProvider.getLimit() / 44.1);
    }

    public void clearLoopPoints(){
        mBufferProvider.clearMark();
        mBufferProvider.clearLimit();
    }
}