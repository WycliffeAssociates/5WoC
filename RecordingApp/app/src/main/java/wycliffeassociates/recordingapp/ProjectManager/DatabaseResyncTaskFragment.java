package wycliffeassociates.recordingapp.ProjectManager;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import wycliffeassociates.recordingapp.database.ProjectDatabaseHelper;

/**
 * Created by sarabiaj on 9/1/2016.
 */
public class DatabaseResyncTaskFragment extends Fragment {

    public interface DatabaseResyncCallback{
        void onDatabaseResynced();
    }

    DatabaseResyncCallback mProgressUpdateCallback;
    Handler mHandler;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        mProgressUpdateCallback = (DatabaseResyncCallback) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onDetach(){
        super.onDetach();
        mProgressUpdateCallback = null;
    }

    public List<File> getAllTakes(){
        File root = new File(Environment.getExternalStorageDirectory(), "TranslationRecorder");
        File[] dirs = root.listFiles();
        List<File> files = new LinkedList<>();
        //files shouldn't be at this level, and the app currently could not handle adding them in this way.
        //skip the visualization folder
        for(File f : dirs){
            if(f.isDirectory() && !f.getName().equals("Visualization")){
                files.addAll(getFilesInDirectory(f.listFiles()));
            }
        }
        return files;
    }

    public List<File> getFilesInDirectory(File[] files){
        List<File> list = new LinkedList<>();
        for(File f : files){
            if(f.isDirectory()) {
                list.addAll(getFilesInDirectory(f.listFiles()));
            } else {
                list.add(f);
            }
        }
        return list;
    }

    public void closeDialog(){
        mProgressUpdateCallback.onDatabaseResynced();
    }

    public void resyncDatabase(){
        Thread dbThread = new Thread(new Runnable() {
            @Override
            public void run() {
                ProjectDatabaseHelper db = new ProjectDatabaseHelper(getActivity());
                db.resyncDbWithFs(getAllTakes());
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        closeDialog();
                    }
                });
            }
        });
        dbThread.start();
    }
}
