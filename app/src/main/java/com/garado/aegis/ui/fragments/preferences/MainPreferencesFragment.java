package com.garado.aegis.ui.fragments.preferences;

import android.content.Intent;
import android.os.Bundle;

import androidx.preference.Preference;

import com.garado.aegis.R;
import com.garado.aegis.ui.AboutActivity;

public class MainPreferencesFragment extends PreferencesFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);

        findPreference("pref_about").setOnPreferenceClickListener(pref -> {
            startActivity(new Intent(requireContext(), AboutActivity.class));
            return true;
        });
    }
}
