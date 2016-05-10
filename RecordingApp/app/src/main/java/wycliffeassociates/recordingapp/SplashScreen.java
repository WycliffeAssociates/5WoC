package wycliffeassociates.recordingapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import wycliffeassociates.recordingapp.SettingsPage.Settings;
import com.door43.login.ProfileActivity;
import com.door43.login.TermsOfUseActivity;
import com.door43.login.core.Profile;

/**
 * Created by sarabiaj on 5/5/2016.
 */
public class SplashScreen extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String profile = PreferenceManager.getDefaultSharedPreferences(this).getString(Settings.KEY_PROFILE, "");
        boolean termsOfUseAccepted = false;
        if(profile.compareTo("") != 0) {
            try {
                termsOfUseAccepted = TermsOfUseActivity.termsAccepted(profile, this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(profile.compareTo("") == 0 || !termsOfUseAccepted) {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra(Profile.PROFILE_KEY, profile);
            startActivityForResult(intent, 42);
        } else {
            startActivity(new Intent(this, MainMenu.class));
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //save the profile in preferences
        if(requestCode == 42 && resultCode == RESULT_OK) {
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString(Settings.KEY_PROFILE, data.getStringExtra(Profile.PROFILE_KEY)).commit();
        //user backed out of profile page; empty the profile and finish
        } else if (resultCode == RESULT_CANCELED){
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString(Settings.KEY_PROFILE, "").commit();
            finish();
        //user backed out of the TOU, keep the profile with unaccepeted TOU and finish. App will resume to TOU page
        } else if (resultCode == TermsOfUseActivity.RESULT_BACKED_OUT_TOU){
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString(Settings.KEY_PROFILE, data.getStringExtra(Profile.PROFILE_KEY)).commit();
            finish();
        //user declined TOU, clear profile and return to profile page
        } else if (resultCode == TermsOfUseActivity.RESULT_DECLINED_TOU){
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString(Settings.KEY_PROFILE, "").commit();
        }
    }
}