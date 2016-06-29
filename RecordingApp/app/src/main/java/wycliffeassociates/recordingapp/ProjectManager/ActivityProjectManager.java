package wycliffeassociates.recordingapp.ProjectManager;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.List;

import wycliffeassociates.recordingapp.FilesPage.FragmentDeleteDialog;
import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.Recording.RecordingScreen;
import wycliffeassociates.recordingapp.SettingsPage.Settings;
import wycliffeassociates.recordingapp.SplashScreen;
import wycliffeassociates.recordingapp.project.ProjectWizardActivity;

/**
 * Created by sarabiaj on 6/23/2016.
 */
public class ActivityProjectManager extends AppCompatActivity implements ProjectInfoDialog.InfoDialogCallback {

    LinearLayout mProjectLayout;
    Button mNewProjectButton;
    FloatingActionButton mAddProject;
    ListView mProjectList;
    SharedPreferences pref;
    ListAdapter mAdapter;
    private int mNumProjects = 0;

    public static final int PROJECT_WIZARD_REQUEST = RESULT_FIRST_USER;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_management);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.project_management_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Project Management");

        pref = PreferenceManager.getDefaultSharedPreferences(this);

        ProjectDatabaseHelper db = new ProjectDatabaseHelper(this);
        mNumProjects = db.getNumProjects();

        initializeViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.project_management_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_logout:
                logout();
                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initializeViews(){
        mProjectLayout = (LinearLayout) findViewById(R.id.project_list_layout);
        mNewProjectButton = (Button) findViewById(R.id.new_project_button);
        mAddProject = (FloatingActionButton) findViewById(R.id.new_project_fab);
        mProjectList = (ListView) findViewById(R.id.project_list);

        mAddProject.setOnClickListener(btnClick);
        mNewProjectButton.setOnClickListener(btnClick);

        if(mNumProjects > 0){
            mNewProjectButton.setVisibility(View.GONE);
            populateProjectList();
        } else {
            mProjectLayout.setVisibility(View.GONE);
        }
    }

    private void populateProjectList(){
        final ProjectDatabaseHelper db = new ProjectDatabaseHelper(this);
        final List<Project> projects = db.getAllProjects();
        projects.get(0);
        mAdapter = new ProjectAdapter(this, projects);

        mProjectList.setAdapter(mAdapter);
//        mProjectList.setDividerHeight(0);
//        mProjectList.setDivider(null);
    }

    //sets the profile in the preferences to "" then returns to the splash screen
    private void logout(){
        pref.edit().putString(Settings.KEY_PROFILE, "").commit();
        finishAffinity();
        Intent intent = new Intent(this, SplashScreen.class);
        startActivity(intent);
    }

    private void createNewProject(){
        startActivityForResult(new Intent(getBaseContext(), ProjectWizardActivity.class), PROJECT_WIZARD_REQUEST);
    }

    private void loadProject(Project project){
        pref.edit().putString("resume", "resume").commit();

        pref.edit().putString(Settings.KEY_PREF_BOOK, project.getSlug()).commit();
        pref.edit().putString(Settings.KEY_PREF_BOOK_NUM, project.getBookNumber()).commit();
        pref.edit().putString(Settings.KEY_PREF_LANG, project.getTargetLanguage()).commit();
        pref.edit().putString(Settings.KEY_PREF_SOURCE, project.getSource()).commit();
        pref.edit().putString(Settings.KEY_PREF_PROJECT, project.getProject()).commit();
        pref.edit().putString(Settings.KEY_PREF_CHUNK_VERSE, project.getMode()).commit();
        pref.edit().putString(Settings.KEY_PREF_LANG_SRC, project.getSourceLanguage()).commit();

        //FIXME: find the last place worked on?
        pref.edit().putString(Settings.KEY_PREF_CHAPTER, "1").commit();
        pref.edit().putString(Settings.KEY_PREF_START_VERSE, "1").commit();
        pref.edit().putString(Settings.KEY_PREF_END_VERSE, "1").commit();
        pref.edit().putString(Settings.KEY_PREF_CHUNK, "1").commit();

        Settings.updateFilename(this);
    }

    private void addProjectToDatabase(Project project){
        ProjectDatabaseHelper db = new ProjectDatabaseHelper(this);
        db.addProject(project);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case PROJECT_WIZARD_REQUEST:{
                if(resultCode == RESULT_OK){
                    Project project = data.getParcelableExtra(Project.PROJECT_EXTRA);
                    addProjectToDatabase(project);
                    loadProject(project);
                    finish();
                    Intent intent = new Intent(this, RecordingScreen.class);
                    startActivity(intent);
                } else {
                    onResume();
                }
            }
            default:
        }
    }

    private View.OnClickListener btnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.new_project_button:
                case R.id.new_project_fab:
                    createNewProject();
                    break;
            }
        }
    };

    @Override
    public void onDelete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete recordings?");
        builder.setIcon(R.drawable.ic_delete_black_36dp);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void showDeleteConfirmDialog(View v) {
        FragmentManager fm = getFragmentManager();
        FragmentDeleteDialog d = new FragmentDeleteDialog();
        d.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        d.show(fm, "Delete Confirm Dialog");
    }
}
