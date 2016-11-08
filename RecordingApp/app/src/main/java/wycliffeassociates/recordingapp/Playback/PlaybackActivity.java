package wycliffeassociates.recordingapp.Playback;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import wycliffeassociates.recordingapp.FilesPage.FileNameExtractor;
import wycliffeassociates.recordingapp.Playback.fragments.FragmentFileBar;
import wycliffeassociates.recordingapp.Playback.fragments.FragmentPlaybackTools;
import wycliffeassociates.recordingapp.Playback.fragments.FragmentTabbedWidget;
import wycliffeassociates.recordingapp.Playback.fragments.WaveformFragment;
import wycliffeassociates.recordingapp.Playback.interfaces.AudioStateCallback;
import wycliffeassociates.recordingapp.Playback.interfaces.MediaController;
import wycliffeassociates.recordingapp.ProjectManager.Project;
import wycliffeassociates.recordingapp.ProjectManager.dialogs.RatingDialog;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.Recording.RecordingScreen;
import wycliffeassociates.recordingapp.Reporting.Logger;
import wycliffeassociates.recordingapp.database.ProjectDatabaseHelper;
import wycliffeassociates.recordingapp.wav.WavFile;
import wycliffeassociates.recordingapp.widgets.FourStepImageView;



/**
 * Created by sarabiaj on 10/27/2016.
 */

public class PlaybackActivity extends Activity implements RatingDialog.DialogListener, MediaController, AudioStateCallback {

    @Override
    public void onPlayerPaused() {
        mFragmentPlaybackTools.onPlayerPaused();
    }



    private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    private static final String KEY_PROJECT = "key_project";
    private static final String KEY_WAV_FILE = "wavfile";
    private static final String KEY_CHAPTER = "key_chapter";
    private static final String KEY_UNIT = "key_unit";

    private volatile boolean isSaved = true;
    private boolean isPlaying = false;
    private boolean isInVerseMarkerMode = false;

    private WavFile mWavFile;
    private Project mProject;
    private int mChapter, mUnit, mRating, mVersesLeft;
    private AudioVisualController mAudioController;
    private HashMap<Integer, Fragment> mFragmentContainerMapping;
    private FragmentPlaybackTools mFragmentPlaybackTools;
    private FragmentTabbedWidget mFragmentTabbedWidget;
    private FragmentFileBar mFragmentFileBar;
    private WaveformFragment mWaveformFragment;

    public static Intent getPlaybackIntent(Context ctx, WavFile file, Project project, int chapter, int unit) {
        Intent intent = new Intent(ctx, PlaybackActivity.class);
        intent.putExtra(KEY_PROJECT, project);
        intent.putExtra(KEY_WAV_FILE, file);
        intent.putExtra(KEY_CHAPTER, chapter);
        intent.putExtra(KEY_UNIT, unit);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_playback_screen);
        initialize(getIntent());
    }

    private void initialize(Intent intent) {
        isSaved = true;
        parseIntent(intent);
        initializeFragments();
        initializeViews();
        mAudioController = new AudioVisualController(this, mWavFile);
    }

    private void parseIntent(Intent intent) {
        mWavFile = intent.getParcelableExtra(KEY_WAV_FILE);
        mProject = intent.getParcelableExtra(KEY_PROJECT);
        mUnit = intent.getIntExtra(KEY_UNIT, 1);
        mChapter = intent.getIntExtra(KEY_CHAPTER, 1);
    }

    private void initializeFragments() {
        mFragmentContainerMapping = new HashMap<>();

        mFragmentPlaybackTools = FragmentPlaybackTools.newInstance();
        mFragmentContainerMapping.put(R.id.playback_tools_fragment_holder, mFragmentPlaybackTools);

        mFragmentTabbedWidget = FragmentTabbedWidget.newInstance(mProject, FileNameExtractor.getNameWithoutTake(mWavFile.getFile().getName()), mChapter);
        mFragmentContainerMapping.put(R.id.tabbed_widget_fragment_holder, mFragmentTabbedWidget);

        mFragmentFileBar = FragmentFileBar.newInstance(mProject.getTargetLanguage(),
                mProject.getVersion(), mProject.getSlug(), "Chapter", String.valueOf(mChapter),
                mProject.getMode(), String.valueOf(mUnit));
        mFragmentContainerMapping.put(R.id.file_bar_fragment_holder, mFragmentFileBar);
        attachFragments();
    }

    private void attachFragments() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Set<Map.Entry<Integer, Fragment>> entrySet = mFragmentContainerMapping.entrySet();
        for(Map.Entry<Integer, Fragment> pair : entrySet) {
            ft.add(pair.getKey(), pair.getValue());
        }
        ft.commit();
    }

//    private void findViews() {
//        mToolbar = (RelativeLayout) findViewById(R.id.toolbar);
//
//        mVerseMarkerCount = (TextView) findViewById(R.id.verse_marker_count);
//        mVerseMarkerLabel = (TextView) findViewById(R.id.verse_marker_label);
//
//
//    }

    private void initializeViews() {

        // By default, select the minimap view over the source playback
        mSwitchToMinimap.setSelected(true);

//        ProjectDatabaseHelper db = new ProjectDatabaseHelper(this);
//        FileNameExtractor fne = new FileNameExtractor(mWavFile.getFile());
//        mRating = db.getTakeRating(fne);
//        mRateBtn.setStep(mRating);
//        mRateBtn.invalidate();
//        db.close();
    }

    @Override
    public void onPlay() {

    }

    @Override
    public void onPause() {
        super.onPause();
        mSrcPlayer.pauseSource();
    }

    @Override
    public void onSeekForward() {

    }

    @Override
    public void onSeekBackward() {

    }

    @Override
    public int getDuration() {
        return 0;
    }

    @Override
    public int getLocation() {
        return 0;
    }

    @Override
    public int setOnCompleteListner(Runnable onComplete) {
        onComplete.run();
        return 0;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSrcPlayer.cleanup();
//        mManager.release();
//        SectionMarkers.clearMarkers(mManager);
    }

    @Override
    public void onPositiveClick(RatingDialog dialog) {
        mRating = dialog.getRating();
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(this);
        db.setTakeRating(new FileNameExtractor(dialog.getTakeName()), mRating);
        db.close();
        //mRateBtn.setStep(mRating);
    }

    @Override
    public void onNegativeClick(RatingDialog dialog) {
        dialog.dismiss();
    }

    @Override
    public void onBackPressed() {
        Logger.i(this.toString(), "Back was pressed.");
//        if (!isSaved && mManager.hasCut()) {
//            Logger.i(this.toString(), "Asking if user wants to save before going back");
//            ExitDialog exit = ExitDialog.Build(this, R.style.Theme_AppCompat_Light_Dialog, true, isPlaying, mWavFile.getFile());
//            exit.show();
//        } else {
////            clearMarkers();
//            mManager.release();
//            super.onBackPressed();
//        }
    }

    private void playRecording() {
//        isPlaying = true;
//        mManager.onPlay();
//        int toShow[] = {R.id.btn_pause};
//        int toHide[] = {R.id.btn_play};
//        mManager.swapViews(toShow, toHide);
//        mManager.updateUI();
    }

    private void pausePlayback() {
//        // NOTE: Shouldn't we set isPlaying = false here?
//        mManager.onPause(true);
//        int toShow[] = {R.id.btn_play};
//        int toHide[] = {R.id.btn_pause};
//        mManager.swapViews(toShow, toHide);
    }

    private void skipForward() {
//        mManager.seekToEnd();
//        mManager.updateUI();
    }

    private void skipBack() {
//        mManager.seekToStart();
//        mManager.updateUI();
    }

    private void placeStartMarker() {
//        mMainCanvas.placeStartMarker(mManager.getLocationMs());
//        int toShow[] = {R.id.btn_end_mark, R.id.btn_clear};
//        int toHide[] = {R.id.btn_start_mark};
//        mManager.swapViews(toShow, toHide);
//        mManager.updateUI();
    }

    private void placeEndMarker() {
//        mMainCanvas.placeEndMarker(mManager.getLocationMs());
//        int toShow[] = {R.id.btn_cut};
//        int toHide[] = {R.id.btn_end_mark};
//        mManager.swapViews(toShow, toHide);
//        mManager.updateUI();
    }

    private void cut() {
//        isSaved = false;
//        int toShow[] = {R.id.btn_start_mark, R.id.btn_undo};
//        int toHide[] = {R.id.btn_cut, R.id.btn_clear};
//        mManager.swapViews(toShow, toHide);
//        mManager.cutAndUpdate();
    }

    private void undo() {
//        // TODO: Check mManager.hasCut() before hiding the undo button when cut is allowed more than one time.
//        mManager.undoCut();
//        int toShow[] = {};
//        int toHide[];
//        if (!mManager.hasCut()) {
//            toHide = new int[1];
//            toHide[0] = R.id.btn_undo;
//        } else {
//            toHide = new int[0];
//        }
//        mManager.swapViews(toShow, toHide);
    }

    private void clearMarkers() {
//        SectionMarkers.clearMarkers(mManager);
//        int toShow[] = {R.id.btn_start_mark};
//        int toHide[] = {R.id.btn_clear, R.id.btn_end_mark, R.id.btn_cut};
//        mManager.swapViews(toShow, toHide);
//        mManager.updateUI();
    }

    private void openRating(FourStepImageView v) {
        RatingDialog dialog = RatingDialog.newInstance(mWavFile.getFile().getName(), mRating);
        dialog.show(getFragmentManager(), "single_unit_rating");
    }

    private void rerecord() {
        Intent intent = RecordingScreen.getRerecordIntent(this, mProject, mWavFile, mChapter, mUnit);
        save(intent);
    }

    private void save(Intent intent) {
        //no changes were made, so just exit
        if (isSaved) {
            if (intent == null) {
                this.finish();
                return;
            } else {
                startActivity(intent);
                this.finish();
                return;
            }
        }

        File dir = new File(Project.getProjectDirectory(mProject), FileNameExtractor.chapterIntToString(mProject, mChapter));
        File from = mWavFile.getFile();
        int takeInt = FileNameExtractor.getLargestTake(dir, from) + 1;
        String take = String.format("%02d", takeInt);
        FileNameExtractor fne = new FileNameExtractor(from);

        File to = new File(dir, fne.getNameWithoutTake() + "_t" + take + AUDIO_RECORDER_FILE_EXT_WAV);
        writeCutToFile(to, mWavFile, intent);
    }

    /**
     * Names the currently recorded .wav file.
     *
     * @return the absolute path of the file created
     */
    public void writeCutToFile(final File to, final WavFile from, final Intent intent) {

//        final ProgressDialog pd = new ProgressDialog(this);
//        pd.setTitle("Saving");
//        pd.setMessage("Writing changes to file, please wait...");
//        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//        pd.setProgressNumberFormat(null);
//        pd.show();
//        Thread saveThread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                if (mManager.hasCut()) {
//                    try {
//                        File dir = Project.getProjectDirectory(mProject);
//                        File toTemp = new File(dir, "temp.wav");
//                        mManager.writeCut(toTemp, from, pd);
//                        to.delete();
//                        toTemp.renameTo(to);
//                        ProjectDatabaseHelper db = new ProjectDatabaseHelper(PlaybackScreen.this);
//                        db.addTake(new FileNameExtractor(to), to.getName(), to.lastModified(), 0);
//                        db.close();
//                        String oldName = from.getFile().getName();
//                        oldName = oldName.substring(0, oldName.lastIndexOf("."));
//                        File toVis = new File(Utils.VISUALIZATION_DIR, oldName + ".vis");
//                        toVis.delete();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//                isSaved = true;
//                pd.dismiss();
//                if (intent == null) {
//                    finish();
//                } else {
//                    WavFile result = new WavFile(to);
//                    intent.putExtra(RecordingScreen.KEY_WAV_FILE, result);
//                    startActivity(intent);
//                    finish();
//                }
//            }
//        });
//        saveThread.start();
    }

    public void insert() {
//        Intent insertIntent = RecordingScreen.getInsertIntent(this, mProject, mWavFile, mChapter, mUnit, mManager.getAdjustedLocation());
//        save(insertIntent);
    }

    private boolean allVersesMarked() {
        return mVersesLeft <= 0;
    }

    private int getVersesLeft() {
        // NOTE: Replace with real code to get the number of verses in a chunk
        int verses = 3;
        // -1 because the first verse marker should be dropped at the beginning automatically
        return verses - 1;
    }

    private void setVerseMarkerCount(int count) {
        // - 1 because the first verse marker should be automatically dropped at the beginning
        mVerseMarkerCount.setText(String.valueOf(count));
    }

//    private View[] getViewsToHideInMarkerMode() {
//        return new View[]{mLangView, mSourceView, mBookView, mChapterView, mChapterLabel,
//                mUnitView, mUnitLabel, mEnterVerseMarkerMode, mRateBtn, mRerecordBtn, mInsertBtn,
//                mDropStartMarkBtn, mSaveBtn, mDropStartMarkBtn};
//    }
//
//    private View[] getViewsToHideInNormalMode() {
//        return new View[]{mExitVerseMarkerMode, mVerseMarkerCount, mVerseMarkerLabel, mDropVerseMarkerBtn,
//                mCompleteVerseMarkerBtn};
    }

//    private void enterVerseMarkerMode() {
//        isInVerseMarkerMode = true;
//        Utils.showView(getViewsToHideInNormalMode());
//        Utils.hideView(getViewsToHideInMarkerMode());
//        Utils.hideView(allVersesMarked() ? mDropVerseMarkerBtn : mCompleteVerseMarkerBtn);
//        mToolbar.setBackgroundColor(getResources().getColor(R.color.tertiary));
//    }
//
//    private void exitVerseMarkerMode() {
//        isInVerseMarkerMode = false;
//        Utils.showView(getViewsToHideInMarkerMode());
//        Utils.hideView(getViewsToHideInNormalMode());
//        mToolbar.setBackgroundColor(getResources().getColor(R.color.primary));
//    }

    private void dropVerseMarker() {
        //mMainCanvas.dropVerseMarker(mManager.getLocationMs());
        //mManager.updateUI();
    }

    private void saveVerseMarkerPosition() {
        // NOTE: Put real code here
        System.out.println("Save verse marker position here");
    }

    private View.OnClickListener btnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_play: {
                    playRecording();
                    break;
                }
                case R.id.btn_save: {
                    save(null);
                    break;
                }
                case R.id.btn_pause: {
                    pausePlayback();
                    break;
                }
                case R.id.btn_skip_forward: {
                    skipForward();
                    break;
                }
                case R.id.btn_skip_back: {
                    skipBack();
                    break;
                }
                case R.id.btn_start_mark: {
                    placeStartMarker();
                    break;
                }
                case R.id.btn_end_mark: {
                    placeEndMarker();
                    break;
                }
                case R.id.btn_cut: {
                    cut();
                    break;
                }
                case R.id.btn_clear: {
                    clearMarkers();
                    break;
                }
                case R.id.btn_rate: {
                    // NOTE: Probably don't need to pass in the view once we implement it the right
                    // way
                    openRating((FourStepImageView) v);
                    break;
                }
                case R.id.btn_undo: {
                    undo();
                    break;
                }
                case R.id.btn_enter_verse_marker_mode: {
                    enterVerseMarkerMode();
                    break;
                }
                case R.id.btn_exit_verse_marker_mode: {
                    exitVerseMarkerMode();
                    break;
                }
                case R.id.btn_drop_verse_marker: {
                    dropVerseMarker();
                    mVersesLeft -= 1;
                    setVerseMarkerCount(mVersesLeft);
                    if (allVersesMarked()) {
                        Utils.showView(mCompleteVerseMarkerBtn);
                        Utils.hideView(mDropVerseMarkerBtn);
                    }
                    break;
                }
                case R.id.btn_verse_marker_done: {
                    saveVerseMarkerPosition();
                    exitVerseMarkerMode();
                    break;
                }
                case R.id.btn_rerecord: {
                    rerecord();
                    break;
                }
                case R.id.btn_insert_record: {
                    insert();
                    break;
                }
                case R.id.switch_minimap: {
                    // TODO: Refactor? Maybe use radio button to select one and exclude the other?
                    v.setSelected(true);
                    v.setBackgroundColor(Color.parseColor("#00000000"));
                    //minimap.setVisibility(View.VISIBLE);
                    mSrcAudioPlayback.setVisibility(View.INVISIBLE);
                    mSwitchToPlayback.setSelected(false);
                    mSwitchToPlayback.setBackgroundColor(getResources().getColor(R.color.mostly_black));
                    break;
                }
                case R.id.switch_source_playback: {
                    // TODO: Refactor? Maybe use radio button to select one and exclude the other?
                    v.setSelected(true);
                    v.setBackgroundColor(Color.parseColor("#00000000"));
                    mSrcAudioPlayback.setVisibility(View.VISIBLE);
                    //minimap.setVisibility(View.INVISIBLE);
                    mSwitchToMinimap.setSelected(false);
                    mSwitchToMinimap.setBackgroundColor(getResources().getColor(R.color.mostly_black));
                    break;
                }
            }
        }
    };
}