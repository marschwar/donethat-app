package de.codekenner.roadtrip.Activities;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by markus on 26.06.13.
 */
public class SettingsActivity extends Activity {

    public static final String KEY_SYNC_SERVER = "pref_syncServer";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}