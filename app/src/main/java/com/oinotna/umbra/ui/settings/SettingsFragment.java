package com.oinotna.umbra.ui.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.oinotna.umbra.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        Preference userButton = (Preference) findPreference("download");
        userButton.setOnPreferenceClickListener(arg0 -> {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/89oinotna/UmbraServer/releases")));
            return true;
        });
    }
}