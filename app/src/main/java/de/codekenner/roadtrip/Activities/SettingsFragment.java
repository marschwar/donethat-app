package de.codekenner.roadtrip.Activities;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import de.codekenner.roadtrip.R;

/**
 * Created by markus on 26.06.13.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}