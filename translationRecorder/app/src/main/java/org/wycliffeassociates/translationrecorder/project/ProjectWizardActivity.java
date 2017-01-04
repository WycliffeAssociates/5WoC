package org.wycliffeassociates.translationrecorder.project;

import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import org.wycliffeassociates.translationrecorder.ProjectManager.Project;
import org.wycliffeassociates.translationrecorder.project.adapters.ProjectCategoryAdapter;

import org.wycliffeassociates.translationrecorder.R;
import org.wycliffeassociates.translationrecorder.Utils;
import org.wycliffeassociates.translationrecorder.project.adapters.ModeCategoryAdapter;
import org.wycliffeassociates.translationrecorder.project.adapters.TargetBookAdapter;
import org.wycliffeassociates.translationrecorder.project.adapters.TargetLanguageAdapter;


/**
 * Created by sarabiaj on 5/27/2016.
 */
public class ProjectWizardActivity extends AppCompatActivity implements ScrollableListFragment.OnItemClickListener {

    protected static final String mProjectKey = "project_key";
    protected static final String mCurrentFragmentKey = "current_fragment_key";
    protected static final String mLastFragmentKey = "last_fragment_key";
    protected static final String mSearchTextKey = "search_text_key";
    protected Project mProject;
    protected ScrollableListFragment mFragment;
    protected String mSearchText;
    protected FragmentManager mFragmentManager;
    private SearchView mSearchViewAction;

    interface ProjectContract {
        String PROJECT_KEY = mProjectKey;
    }

    public static final int BASE_PROJECT = 1;
    public static final int TARGET_LANGUAGE = BASE_PROJECT;
    public static final int PROJECT = BASE_PROJECT + 1;
    public static final int BOOK = BASE_PROJECT + 2;
    public static final int SOURCE_TEXT = BASE_PROJECT + 3;
    public static final int MODE = BASE_PROJECT + 4;
    public static final int SOURCE_LANGUAGE = BASE_PROJECT + 5;
    private int mCurrentFragment = BASE_PROJECT;
    private int mLastFragment;

    private static final int SOURCE_AUDIO_REQUEST = 42;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragmentManager = getFragmentManager();

        this.displayFragment();
        mProject = new Project();

        setContentView(R.layout.activity_scrollable_list);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("New Project");
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(mProjectKey, mProject);
        outState.putInt(mCurrentFragmentKey, mCurrentFragment);
        outState.putInt(mLastFragmentKey, mLastFragment);
        outState.putString(mSearchTextKey, mSearchText);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mProject = savedInstanceState.getParcelable(mProjectKey);
        mCurrentFragment = savedInstanceState.getInt(mCurrentFragmentKey);
        mLastFragment = savedInstanceState.getInt(mLastFragmentKey);
        mSearchText = savedInstanceState.getString(mSearchTextKey);
        displayFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.language_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_update).setVisible(false);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        mSearchViewAction = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        mSearchViewAction.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                mSearchText = s;
                mFragment.onSearchQuery(s);
                return true;
            }
        });
        mSearchViewAction.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        if (mSearchText != null) {
            mSearchViewAction.setQuery(mSearchText, true);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SOURCE_AUDIO_REQUEST && resultCode == RESULT_OK) {
            mProject = data.getParcelableExtra(Project.PROJECT_EXTRA);
            Intent intent = new Intent();
            intent.putExtra(Project.PROJECT_EXTRA, mProject);
            setResult(RESULT_OK, intent);
            finish();
        } else if (resultCode == RESULT_CANCELED) {
            mCurrentFragment = mLastFragment;
            this.displayFragment();
        }
    }

    private void clearSearchState() {
        if (mSearchViewAction != null) {
            mSearchText = "";
            mSearchViewAction.onActionViewCollapsed();
        }
    }

    @Override
    public void onItemClick(Object result) {
        clearSearchState();
        Utils.closeKeyboard(this);
        if (mCurrentFragment == TARGET_LANGUAGE && result instanceof Language) {
            ((Project) mProject).setTargetLanguage(((Language) result).getCode());
            mCurrentFragment++;
            this.displayFragment();
        } else if (mCurrentFragment == PROJECT && result instanceof String) {
            String project = "";
            if (((String) result).compareTo("Bible: OT") == 0) {
                project = "ot";
            } else if (((String) result).compareTo("Bible: NT") == 0) {
                project = "nt";
            } else {
                project = "obs";
            }
            ((Project) mProject).setProject(project);
            mLastFragment = mCurrentFragment;
            mCurrentFragment = project.compareTo("obs") == 0 ? SOURCE_LANGUAGE : BOOK;
            this.displayFragment();
        } else if (mCurrentFragment == BOOK && result instanceof Book) {
            Book book = (Book) result;
            mProject.setBookNumber(book.getOrder());
            mProject.setSlug(book.getSlug());
            mCurrentFragment++;
            this.displayFragment();
        } else if (mCurrentFragment == SOURCE_TEXT && result instanceof String) {
            String source = "";
            if (((String) result).compareTo("Unlocked Literal Bible") == 0) {
                source = "ulb";
            } else if (((String) result).compareTo("Unlocked Dynamic Bible") == 0) {
                source = "udb";
            } else {
                source = "reg";
            }
            ((Project) mProject).setVersion((String) source);
            mCurrentFragment++;
            this.displayFragment();
        } else if (mCurrentFragment == MODE && result instanceof String) {
            ((Project) mProject).setMode(((String) result).toLowerCase());
            mLastFragment = mCurrentFragment;
            mCurrentFragment++;
            this.displayFragment();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        clearSearchState();
        Utils.closeKeyboard(this);
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mCurrentFragment > TARGET_LANGUAGE) {
                    mCurrentFragment--;
                    this.displayFragment();
                } else {
                    this.finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void displayFragment() {
        // Remove old fragment, if there's any
        if (mFragment != null) {
            mFragmentManager.beginTransaction().remove(mFragment).commit();
        }
        // Build a new fragment based on the current step
        switch (mCurrentFragment) {
            case TARGET_LANGUAGE:
                mFragment = new ScrollableListFragment
                        .Builder(new TargetLanguageAdapter(Language.getLanguages(this), this))
                        .setSearchHint("Choose Target Language:")
                        .build();
                break;
            case PROJECT:
                mFragment = new ScrollableListFragment
                        //.Builder(new ProjectCategoryAdapter(new String[]{"Bible: OT", "Bible: NT", "Open Bible Stories"}, this))
                        .Builder(new ProjectCategoryAdapter(new String[]{"Bible: OT", "Bible: NT"}, this))
                        .setSearchHint("Choose a Project")
                        .build();
                break;
            case BOOK:
                mFragment = new ScrollableListFragment
                        .Builder(new TargetBookAdapter(ParseJSON.getBooks(this, mProject.getAnthology()), this))
                        .setSearchHint("Choose a Book")
                        .build();
                break;
            case SOURCE_TEXT:
                mFragment = new ScrollableListFragment
                        .Builder(new ProjectCategoryAdapter(new String[]{"Unlocked Literal Bible", "Unlocked Dynamic Bible", "Regular"}, this))
                        .setSearchHint("Choose Translation Type")
                        .build();
                break;
            case MODE:
                mFragment = new ScrollableListFragment
                        .Builder(new ModeCategoryAdapter(new String[]{"Verse", "Chunk"}, this))
                        .setSearchHint("Choose a Mode")
                        .build();
                break;
            default:
                mFragment = null;
                break;
        }
        if (mFragment != null) {
            // Display fragment if a new one is built
            mFragmentManager.beginTransaction().add(R.id.fragment_container, mFragment).commit();
        } else {
            // Route to SourceAudioActivity if there's no new fragment
            Intent intent = new Intent(this, SourceAudioActivity.class);
            intent.putExtra(Project.PROJECT_EXTRA, mProject);
            startActivityForResult(intent, SOURCE_AUDIO_REQUEST);
        }
    }
}
