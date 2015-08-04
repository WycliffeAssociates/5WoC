package wycliffeassociates.recordingapp;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;


import wycliffeassociates.recordingapp.model.AudioItem;

public class AudioFiles extends Activity {

    private DrawerLayout mDrawerLayout;

    private CheckBox btnCheckAll;

    private ImageButton
                btnSortName, btnSortDuration, btnSortDate, btnDelete,
            btnExport,
                btnExportApp, btnExportFTP, btnExportFolder;

    private ListView audioFileView;
    private TextView file_path;
    private static String currentDir;
    private File file[];

    private ArrayList<AudioItem> audioItemList;
    private ArrayList<AudioItem> tempItemList;
    static ArrayList<String> exportList;

    private boolean checkAll = true;

    //0, Z-A
    //1, A-Z
    //2, 0:00 - 9:99
    //3, 9:99 - 0:00
    //4, Oldest First
    //5, Recent First
    int sort = 5;

    public AudioFilesAdapter adapter;

    Hashtable<Date, String> audioHash;

    /**
     * the current filepath being exported
     */
    private String thisPath;

    /**
     * the total number of files being exported
     */
    private int totalFiles = 0;

    /**
     * the number of the current file being exported (corresponds with allMoving)
     */
    private int fileNum =0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_list);


        //pull file directory and sorting preferences
        final PreferencesManager pref = new PreferencesManager(this);
        currentDir = (String) pref.getPreferences("fileDirectory");
        sort = (int) pref.getPreferences("displaySort");


        audioFileView = (ListView) findViewById(R.id.listViewExport);
        file_path = (TextView)findViewById(R.id.filepath);
        file_path.setText(currentDir);

        //initialization
        audioItemList = new ArrayList<AudioItem>();
        tempItemList = new ArrayList<AudioItem>();
        audioHash = new Hashtable<Date, String>();

        //get files in the directory
        File f = new File(currentDir);
        file = f.listFiles();
        //no files
        if (file == null) {
            Toast.makeText(AudioFiles.this, "No Audio Files in Folder", Toast.LENGTH_SHORT).show();
        }
        //get audio files
        else {
            for (int i = 0; i < file.length; i++) {
                int len = file[i].getName().length();
                String sub = file[i].getName().substring(len - 4);

                if (sub.equalsIgnoreCase(".3gp") || sub.equalsIgnoreCase(".wav")
                        || sub.equalsIgnoreCase(".mp3")) {
                    //add file names
                    Date lastModDate = new Date(file[i].lastModified());

                    File tFile = new File (currentDir + "/" + file[i].getName());
                    Uri uri = Uri.fromFile(tFile);

                    //String mediaPath = Uri.parse("android.resource://<your-package-name>/raw/filename").getPath();
                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    mmr.setDataSource(this, uri);
                    String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                    //System.out.println(duration);
                    int time = (Integer.parseInt(duration) / 1000);
                    mmr.release();

                    //create an Audio Item
                    tempItemList.add(new AudioItem(file[i].getName(), lastModDate, time));
                }
            }


            //audioFileView.setAdapter(
            generateAdapterView(tempItemList, sort);
            //);

        }
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        //move this to AudioFilesAdapter -- ultimately to AudioFilesListener

        btnExport = (ImageButton)findViewById(R.id.btnExport);
        btnExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(Gravity.RIGHT);
            }
        });

        btnExportFTP = (ImageButton)findViewById(R.id.btnExportFTP);
        btnExportFTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                exportList = new ArrayList<String>();
                if ((file == null)) {
                    Toast.makeText(AudioFiles.this, "Failed", Toast.LENGTH_SHORT).show();
                }
                else {
                    for (int i = 0; i < adapter.checkBoxState.length; i++) {
                        if (adapter.checkBoxState[i] == true) {
                            exportList.add(pref.getPreferences("fileDirectory") + "/" + audioItemList.get(i).getName());
                        }
                    }
                    if (exportList.size() > 0) {
                        Intent intent = new Intent(v.getContext(), FTPActivity.class);
                        startActivityForResult(intent, 0);
                    } else {
                        Toast.makeText(AudioFiles.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btnExportFolder = (ImageButton)findViewById(R.id.btnExportFolder);
        btnExportFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportList = new ArrayList<String>();
                if ((file == null)) {
                    Toast.makeText(AudioFiles.this, "Failed", Toast.LENGTH_SHORT).show();
                }
                else {
                    for (int i = 0; i < adapter.checkBoxState.length; i++) {
                        if (adapter.checkBoxState[i] == true) {
                            exportList.add(pref.getPreferences("fileDirectory") + "/" + audioItemList.get(i).getName());
                        }
                    }
//                    Intent intent = new Intent(v.getContext(), ExportFiles.class);
                    if (exportList.size() > 0) {
//                        intent.putExtra("exportList", exportList);
//                        startActivityForResult(intent, 0);
                        totalFiles = exportList.size();
                        thisPath = exportList.get(0);
                        createFile("audio/*", getNameFromPath(thisPath));
                    } else {
                        Toast.makeText(AudioFiles.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btnExportApp = (ImageButton)findViewById(R.id.btnExportApp);
        btnExportApp.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                exportList = new ArrayList<String>();
                for (int i = 0; i < adapter.checkBoxState.length; i++) {
                    if (adapter.checkBoxState[i] == true) {
                        exportList.add(pref.getPreferences("fileDirectory") + "/" + audioItemList.get(i).getName());
                    }
                }

                //if something is checked
                if(exportList.size() > 0) {
                    exportApplications(exportList);
                }
                else {
                    Toast.makeText(AudioFiles.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCheckAll = (CheckBox)findViewById(R.id.btnCheckAll);
        btnCheckAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (file == null) {

                } else {
                    for (int i = 0; i < audioFileView.getCount(); i++) {
                        adapter.checkBoxState[i] = checkAll;
                        adapter.notifyDataSetChanged();
                    }
                    if (checkAll == false) {
                        exportList = new ArrayList<String>();
                        Arrays.fill(adapter.checkBoxState, Boolean.FALSE);
                        adapter.notifyDataSetChanged();
                    }
                    checkAll = !checkAll;
                }
            }
        });

        btnSortName = (ImageButton)findViewById(R.id.btnSortName);
        btnSortName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sort == 1){
                    pref.setPreferences("displaySort", 0);
                }else{
                    pref.setPreferences("displaySort", 1);
                }
                sort = (int) pref.getPreferences("displaySort");
                generateAdapterView(tempItemList, sort);
            }
        });

        btnSortDuration = (ImageButton)findViewById(R.id.btnSortDuration);
        btnSortDuration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sort == 3){
                    pref.setPreferences("displaySort", 2);
                }else{
                    pref.setPreferences("displaySort", 3);
                }
                sort = (int) pref.getPreferences("displaySort");
                generateAdapterView(tempItemList, sort);
            }
        });

        btnSortDate = (ImageButton)findViewById(R.id.btnSortDate);
        btnSortDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sort == 5){
                    pref.setPreferences("displaySort", 4);
                }else{
                    pref.setPreferences("displaySort", 5);
                }
                sort = (int) pref.getPreferences("displaySort");
                generateAdapterView(tempItemList, sort);
            }
        });

        btnDelete = (ImageButton)findViewById(R.id.btnDelete);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     *Clears Check Box State when the back button is pressed
     */
    public void onBackPressed(){
        if (file == null) {}
        else {
            exportList = new ArrayList<String>();
            Arrays.fill(adapter.checkBoxState, Boolean.FALSE);
        }
        finish();
    }

    private void generateAdapterView(ArrayList<AudioItem> tempItemList, int sort){
        ArrayList<AudioItem> cleanList = new ArrayList<AudioItem>();
        for( int a = 0 ; a < tempItemList.size() ; a ++ ){
            cleanList.add(tempItemList.get(a));
        }

        //clear list
        audioItemList = new ArrayList<AudioItem>();

        audioItemList = sortAudioItem(cleanList, sort);

        //temp array for Adapter
        AudioItem[] tempArr = new AudioItem[audioItemList.size()];
        for(int a = 0; a < audioItemList.size(); a++){
            tempArr[a] = audioItemList.get(a);
        }
        //set Adapter view
        adapter = (new AudioFilesAdapter(this, tempArr));
        audioFileView.setAdapter(adapter);
    }

    private ArrayList<AudioItem> sortAudioItem(ArrayList<AudioItem> nList, int sort) {
        ArrayList<AudioItem> outputList = new ArrayList<AudioItem>();

        boolean flag = false;
        switch(sort){
            case 0:
            case 2:
            case 4:
                //false
                break;
            case 1:
            case 3:
            case 5:
            default:
                flag = true;
                break;
        }

        int val = 0;
        int size = nList.size() - 1;

        if(sort == 0 || sort == 1){
            String cmp = "";

            //as long as there are items
            do{
                size = nList.size()-1;
                cmp = nList.get(size).getName().toLowerCase();
                val = size;

                //compare with other items
                for(int x = 0; x < size; x++){
                    if (cmp.compareTo(nList.get(x).getName().toLowerCase()) < 0) {
                        if(flag){
                            //A-Z
                        }else{
                            //Z-A
                            val = x;
                            cmp = nList.get(x).getName();
                        }
                    }else{
                        if(flag) {
                            //A-Z
                            val = x;
                            cmp = nList.get(x).getName();
                        }else{
                            //Z-A
                        }
                    }
                }

                outputList.add(nList.get(val));
                nList.remove(val);

            }while(size > 0);

        }else if(sort == 2 || sort == 3){
            Integer cmp = 0;
            //as long as there are items
            do {
                size = nList.size() - 1;
                cmp = nList.get(size).getDuration();
                val = size;

                //compare with other items
                for (int x = 0; x < size; x++) {
                    if (cmp > nList.get(x).getDuration()) {
                        if (flag) {
                            //A-Z
                        } else {
                            //Z-A
                            val = x;
                            cmp = nList.get(x).getDuration();
                        }
                    } else {
                        if (flag) {
                            //A-Z
                            val = x;
                            cmp = nList.get(x).getDuration();
                        } else {
                            //Z-A
                        }
                    }
                }

                outputList.add(nList.get(val));
                nList.remove(val);

            } while (size > 0);
        }else{
            ArrayList<Date> tempList = new ArrayList<Date>();
            Date cmp = new Date();

            //as long as there are items
            do {
                size = nList.size() - 1;
                cmp = nList.get(size).getDate();
                val = size;

                //compare with other items
                for (int x = 0; x < size; x++) {
                    if (cmp.after(nList.get(x).getDate())) {
                        if (flag) {
                            //A-Z
                        } else {
                            //Z-A
                            val = x;
                            cmp = nList.get(x).getDate();
                        }
                    } else {
                        if (flag) {
                            //A-Z
                            val = x;
                            cmp = nList.get(x).getDate();
                        } else {
                            //Z-A
                        }
                    }
                }

                outputList.add(nList.get(val));
                nList.remove(val);

            } while (size > 0);
        }


        return outputList;
    }

    //Change
    public static void AudioPlay (String fileName){
        WavPlayer.play(currentDir + "/" + fileName);
    }

    //==================================
    //      Export to Applications
    //==================================

    /**
     *  Passes URIs to relevant audio applications.
     *
     *      @param exportList
     *          a list of filenames to be exported
     */
    private void exportApplications(ArrayList<String> exportList){

        Intent sendIntent = new Intent();
        sendIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        //share it implementation
        File tFile;

        //individual file
        if(exportList.size() < 2){
            Uri audioUri;

            tFile = new File (exportList.get(0));
            audioUri = Uri.fromFile(tFile);
            sendIntent.setAction(Intent.ACTION_SEND);

            //send individual URI
            sendIntent.putExtra(Intent.EXTRA_STREAM, audioUri);

            //multiple files
        }else{

            ArrayList<Uri> audioUris = new ArrayList<Uri>();
            for(int i=0; i<exportList.size(); i++){
                tFile = new File(exportList.get(i));
                audioUris.add(Uri.fromFile(tFile));
            }
            sendIntent.setAction(Intent.ACTION_SEND_MULTIPLE);

            //send multiple arrayList of URIs
            sendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, audioUris);
        }

        //open
        sendIntent.setType("audio/*");
        startActivity(Intent.createChooser(sendIntent, "Export Audio"));
    }

    //==================================
    //         Export to Folder
    //==================================

    /**
     * Iterates the file number that is being looked a
     * @return Returns true if iteration worked, false if the end has been reached
     */
    public boolean iteratePath(){
        if(fileNum + 1 < totalFiles) {
            fileNum++;
            return true;
        }
        if(fileNum + 1 == totalFiles){
            fileNum++;
            return false;
        }
        return false;
    }

    /**
     * A method to extract filename from the path
     * @param path The paths to the files
     * @return The simple filename of the file
     */
    public String getNameFromPath(String path){
        String[] temp = path.split("/");
        return temp[temp.length-1];
    }

    /**
     * Copies a file from a path to a uri
     * @param destUri The desination of the file
     * @param path The original path to the file
     */
    public void savefile(Uri destUri, String path)
    {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            String sourceFilename = path;
            ParcelFileDescriptor destinationFilename = getContentResolver().
                    openFileDescriptor(destUri, "w");
            bis = new BufferedInputStream(new FileInputStream(sourceFilename));
            bos = new BufferedOutputStream(new FileOutputStream(destinationFilename.getFileDescriptor()));
            byte[] buf = new byte[1024];
            bis.read(buf);
            do {
                bos.write(buf);
            } while(bis.read(buf) != -1);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null) bis.close();
                if (bos != null) bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            iteratePath();
            if(fileNum < totalFiles) {
                thisPath = exportList.get(fileNum);
                createFile("audio/*", getNameFromPath(thisPath));
            }
        }

    }

    /**
     * Creates a file in folder selected by user
     * @param mimeType Typically going to be "audio/*" for this app
     * @param fileName The name of the file selected.
     */
    private void createFile(String mimeType, String fileName) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT );
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        this.startActivityForResult(intent, 43);
    }

    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        Uri currentUri = null;
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 43) {
                currentUri = resultData.getData();
                savefile(currentUri, exportList.get(fileNum));

            }
        }
    }
}

