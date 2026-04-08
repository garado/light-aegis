package com.garado.aegis.ui.fragments.preferences;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garado.aegis.R;
import com.garado.aegis.ui.AboutActivity;

public class MainPreferencesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_preferences, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.pref_appearance).setOnClickListener(v -> navigate(new AppearancePreferencesFragment(), getString(R.string.pref_section_appearance_title)));
        view.findViewById(R.id.pref_behavior).setOnClickListener(v -> navigate(new BehaviorPreferencesFragment(), getString(R.string.pref_section_behavior_title)));
        view.findViewById(R.id.pref_icon_packs).setOnClickListener(v -> navigate(new IconPacksManagerFragment(), getString(R.string.pref_section_icon_packs)));
        view.findViewById(R.id.pref_security).setOnClickListener(v -> navigate(new SecurityPreferencesFragment(), getString(R.string.pref_section_security_title)));
        view.findViewById(R.id.pref_backups).setOnClickListener(v -> navigate(new BackupsPreferencesFragment(), getString(R.string.pref_section_backups_title)));
        view.findViewById(R.id.pref_import_export).setOnClickListener(v -> navigate(new ImportExportPreferencesFragment(), getString(R.string.pref_section_import_export_title)));
        view.findViewById(R.id.pref_audit_log).setOnClickListener(v -> navigate(new AuditLogPreferencesFragment(), getString(R.string.pref_section_audit_log_title)));
        view.findViewById(R.id.pref_about).setOnClickListener(v -> startActivity(new Intent(requireContext(), AboutActivity.class)));
    }

    private void navigate(Fragment fragment, String title) {
        requireActivity().setTitle(title);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.content, fragment)
                .addToBackStack(null)
                .commit();
    }
}
