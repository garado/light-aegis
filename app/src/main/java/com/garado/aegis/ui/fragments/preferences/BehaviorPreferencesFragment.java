package com.garado.aegis.ui.fragments.preferences;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.garado.aegis.CopyBehavior;
import com.garado.aegis.Preferences;
import com.garado.aegis.R;
import com.garado.aegis.database.AuditLogRepository;
import com.garado.aegis.ui.components.light.LightSelectorButton;
import com.garado.aegis.ui.components.light.LightToggleSwitch;
import com.garado.aegis.ui.dialogs.Dialogs;
import com.garado.aegis.vault.VaultManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BehaviorPreferencesFragment extends Fragment {

    @Inject Preferences _prefs;
    @Inject VaultManager _vaultManager;
    @Inject AuditLogRepository _auditLogRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_preferences_behavior, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Focus search
        LightToggleSwitch focusSearchSwitch = view.findViewById(R.id.pref_focus_search);
        focusSearchSwitch.setCheckedSilently(_prefs.getFocusSearchEnabled());
        focusSearchSwitch.setOnCheckedChangeListener(checked -> _prefs.setFocusSearch(checked));

        // Search behavior
        LightSelectorButton searchBehaviorBtn = view.findViewById(R.id.pref_search_behavior);
        searchBehaviorBtn.setValue(getSearchBehaviorSummary());
        searchBehaviorBtn.setOnClickListener(v -> {
            final int[] items = Preferences.SEARCH_BEHAVIOR_SETTINGS;
            final String[] textItems = getResources().getStringArray(R.array.pref_search_behavior_types);
            final boolean[] checkedItems = new boolean[items.length];
            for (int i = 0; i < items.length; i++) {
                checkedItems[i] = _prefs.isSearchBehaviorTypeEnabled(items[i]);
            }

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.pref_search_behavior_prompt)
                    .setMultiChoiceItems(textItems, checkedItems, (dialog, index, isChecked) -> {
                        checkedItems[index] = isChecked;
                        boolean atLeastOne = false;
                        for (boolean b : checkedItems) { if (b) { atLeastOne = true; break; } }
                        ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(atLeastOne);
                    })
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        int searchBehavior = 0;
                        for (int i = 0; i < checkedItems.length; i++) {
                            if (checkedItems[i]) searchBehavior |= items[i];
                        }
                        _prefs.setSearchBehaviorMask(searchBehavior);
                        searchBehaviorBtn.setValue(getSearchBehaviorSummary());
                    })
                    .setNegativeButton(android.R.string.cancel, null);
            Dialogs.showSecureDialog(builder.create());
        });

        // Minimize on copy
        LightToggleSwitch minimizeOnCopySwitch = view.findViewById(R.id.pref_minimize_on_copy);
        minimizeOnCopySwitch.setCheckedSilently(_prefs.isMinimizeOnCopyEnabled());
        minimizeOnCopySwitch.setOnCheckedChangeListener(checked ->
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                    .edit().putBoolean("pref_minimize_on_copy", checked).apply()
        );

        // Copy behavior
        LightSelectorButton copyBehaviorBtn = view.findViewById(R.id.pref_copy_behavior);
        copyBehaviorBtn.setValue(getResources().getStringArray(R.array.copy_behavior_titles)[_prefs.getCopyBehavior().ordinal()]);
        copyBehaviorBtn.setOnClickListener(v -> {
            int current = _prefs.getCopyBehavior().ordinal();
            Dialogs.showSecureDialog(new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.choose_copy_behavior))
                    .setSingleChoiceItems(R.array.copy_behavior_titles, current, (dialog, which) -> {
                        int i = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        _prefs.setCopyBehavior(CopyBehavior.fromInteger(i));
                        copyBehaviorBtn.setValue(getResources().getStringArray(R.array.copy_behavior_titles)[i]);
                        dialog.dismiss();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create());
        });

        // Haptic feedback
        LightToggleSwitch hapticSwitch = view.findViewById(R.id.pref_haptic_feedback);
        hapticSwitch.setCheckedSilently(_prefs.isHapticFeedbackEnabled());
        hapticSwitch.setOnCheckedChangeListener(checked ->
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                    .edit().putBoolean("pref_haptic_feedback", checked).apply()
        );

        // Groups multiselect
        LightToggleSwitch groupsMultiselectSwitch = view.findViewById(R.id.pref_groups_multiselect);
        groupsMultiselectSwitch.setCheckedSilently(_prefs.isGroupMultiselectEnabled());
        groupsMultiselectSwitch.setOnCheckedChangeListener(checked ->
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                    .edit().putBoolean("pref_groups_multiselect", checked).apply()
        );

        // Highlight entry (affects pause_entry enable state)
        LightToggleSwitch pauseEntrySwitch = view.findViewById(R.id.pref_pause_entry);
        pauseEntrySwitch.setCheckedSilently(_prefs.isPauseFocusedEnabled());
        pauseEntrySwitch.setEnabled(_prefs.isTapToRevealEnabled() || _prefs.isEntryHighlightEnabled());
        pauseEntrySwitch.setOnCheckedChangeListener(checked ->
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                    .edit().putBoolean("pref_pause_entry", checked).apply()
        );

        LightToggleSwitch highlightEntrySwitch = view.findViewById(R.id.pref_highlight_entry);
        highlightEntrySwitch.setCheckedSilently(_prefs.isEntryHighlightEnabled());
        highlightEntrySwitch.setOnCheckedChangeListener(checked -> {
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                    .edit().putBoolean("pref_highlight_entry", checked).apply();
            pauseEntrySwitch.setEnabled(_prefs.isTapToRevealEnabled() || checked);
        });
    }

    private String getSearchBehaviorSummary() {
        final int[] settings = Preferences.SEARCH_BEHAVIOR_SETTINGS;
        final String[] descriptions = getResources().getStringArray(R.array.pref_search_behavior_types);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < settings.length; i++) {
            if (_prefs.isSearchBehaviorTypeEnabled(settings[i])) {
                if (builder.length() != 0) builder.append(", ");
                builder.append(descriptions[i].toLowerCase());
            }
        }
        return getString(R.string.pref_search_behavior_summary, builder.toString());
    }
}
