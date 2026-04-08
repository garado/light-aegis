package com.garado.aegis.ui.fragments.preferences;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.garado.aegis.AccountNamePosition;
import com.garado.aegis.Preferences;
import com.garado.aegis.R;
import com.garado.aegis.Theme;
import com.garado.aegis.ViewMode;
import com.garado.aegis.database.AuditLogRepository;
import com.garado.aegis.ui.GroupManagerActivity;
import com.garado.aegis.ui.components.light.LightButton;
import com.garado.aegis.ui.components.light.LightSelectorButton;
import com.garado.aegis.ui.components.light.LightToggleSwitch;
import com.garado.aegis.ui.dialogs.Dialogs;
import com.garado.aegis.vault.VaultManager;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AppearancePreferencesFragment extends Fragment {

    @Inject Preferences _prefs;
    @Inject VaultManager _vaultManager;
    @Inject AuditLogRepository _auditLogRepository;

    private LightSelectorButton _accountNamePositionView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_preferences_appearance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Groups
        LightButton groupsBtn = view.findViewById(R.id.pref_groups);
        groupsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), GroupManagerActivity.class);
            startActivity(intent);
        });

        // Reset usage count
        LightButton resetUsageBtn = view.findViewById(R.id.pref_reset_usage_count);
        resetUsageBtn.setOnClickListener(v ->
            Dialogs.showSecureDialog(new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.preference_reset_usage_count)
                    .setMessage(R.string.preference_reset_usage_count_dialog)
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> _prefs.clearUsageCount())
                    .setNegativeButton(android.R.string.no, null)
                    .create())
        );

        // Dark mode / theme
        LightSelectorButton darkModeBtn = view.findViewById(R.id.pref_dark_mode);
        darkModeBtn.setValue(getResources().getStringArray(R.array.theme_titles)[_prefs.getCurrentTheme().ordinal()]);
        darkModeBtn.setOnClickListener(v -> {
            int current = _prefs.getCurrentTheme().ordinal();
            Dialogs.showSecureDialog(new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.choose_theme)
                    .setSingleChoiceItems(R.array.theme_titles, current, (dialog, which) -> {
                        int i = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        _prefs.setCurrentTheme(Theme.fromInteger(i));
                        dialog.dismiss();
                        requireActivity().recreate();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create());
        });

        // Dynamic colors
        LightToggleSwitch dynamicColorsSwitch = view.findViewById(R.id.pref_dynamic_colors);
        dynamicColorsSwitch.setCheckedSilently(_prefs.isDynamicColorsEnabled());
        dynamicColorsSwitch.setEnabled(DynamicColors.isDynamicColorAvailable());
        dynamicColorsSwitch.setOnCheckedChangeListener(checked -> {
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                    .edit().putBoolean("pref_dynamic_colors", checked).apply();
            requireActivity().recreate();
        });

        // Language
        LightSelectorButton langBtn = view.findViewById(R.id.pref_lang);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            String[] langs = getResources().getStringArray(R.array.pref_lang_values);
            String[] langNames = getResources().getStringArray(R.array.pref_lang_entries);
            List<String> langList = Arrays.asList(langs);
            int curLangIndex = langList.contains(_prefs.getLanguage()) ? langList.indexOf(_prefs.getLanguage()) : 0;
            langBtn.setValue(langNames[curLangIndex]);
            langBtn.setOnClickListener(v -> {
                int cur = langList.contains(_prefs.getLanguage()) ? langList.indexOf(_prefs.getLanguage()) : 0;
                Dialogs.showSecureDialog(new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.pref_lang_title)
                        .setSingleChoiceItems(langNames, cur, (dialog, which) -> {
                            int newIdx = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                            _prefs.setLanguage(langs[newIdx]);
                            langBtn.setValue(langNames[newIdx]);
                            dialog.dismiss();
                            requireActivity().recreate();
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create());
            });
        } else {
            langBtn.setVisibility(View.GONE);
        }

        // View mode
        LightSelectorButton viewModeBtn = view.findViewById(R.id.pref_view_mode);
        viewModeBtn.setValue(getResources().getStringArray(R.array.view_mode_titles)[_prefs.getCurrentViewMode().ordinal()]);
        viewModeBtn.setOnClickListener(v -> {
            int current = _prefs.getCurrentViewMode().ordinal();
            Dialogs.showSecureDialog(new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.choose_view_mode)
                    .setSingleChoiceItems(R.array.view_mode_titles, current, (dialog, which) -> {
                        int i = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        _prefs.setCurrentViewMode(ViewMode.fromInteger(i));
                        viewModeBtn.setValue(getResources().getStringArray(R.array.view_mode_titles)[i]);
                        refreshAccountNamePositionText();
                        dialog.dismiss();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create());
        });

        // Show icons
        LightToggleSwitch showIconsSwitch = view.findViewById(R.id.pref_show_icons);
        showIconsSwitch.setCheckedSilently(_prefs.isIconVisible());
        showIconsSwitch.setOnCheckedChangeListener(checked ->
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                    .edit().putBoolean("pref_show_icons", checked).apply()
        );

        // Show next code
        LightToggleSwitch showNextCodeSwitch = view.findViewById(R.id.pref_show_next_code);
        showNextCodeSwitch.setCheckedSilently(_prefs.getShowNextCode());
        showNextCodeSwitch.setOnCheckedChangeListener(checked ->
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                    .edit().putBoolean("pref_show_next_code", checked).apply()
        );

        // Expiration state
        LightToggleSwitch expirationSwitch = view.findViewById(R.id.pref_expiration_state);
        expirationSwitch.setCheckedSilently(_prefs.getShowExpirationState());
        expirationSwitch.setOnCheckedChangeListener(checked ->
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                    .edit().putBoolean("pref_expiration_state", checked).apply()
        );

        // Code group size
        String[] codeGroupings = getResources().getStringArray(R.array.pref_code_groupings_values);
        String[] codeGroupingNames = getResources().getStringArray(R.array.pref_code_groupings);
        LightSelectorButton codeGroupBtn = view.findViewById(R.id.pref_code_group_size);
        codeGroupBtn.setOnClickListener(v -> {
            int current = Arrays.asList(codeGroupings).indexOf(_prefs.getCodeGroupSize().name());
            Dialogs.showSecureDialog(new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.pref_code_group_size_title)
                    .setSingleChoiceItems(codeGroupingNames, current, (dialog, which) -> {
                        int newIdx = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        _prefs.setCodeGroupSize(Preferences.CodeGrouping.valueOf(codeGroupings[newIdx]));
                        dialog.dismiss();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create());
        });

        // Account name position
        _accountNamePositionView = view.findViewById(R.id.pref_account_name_position);
        _accountNamePositionView.setOnClickListener(v -> {
            int current = _prefs.getAccountNamePosition().ordinal();
            Dialogs.showSecureDialog(new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.choose_account_name_position))
                    .setSingleChoiceItems(R.array.account_name_position_titles, current, (dialog, which) -> {
                        int i = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        _prefs.setAccountNamePosition(AccountNamePosition.fromInteger(i));
                        refreshAccountNamePositionText();
                        dialog.dismiss();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create());
        });
        refreshAccountNamePositionText();

        // Shared issuer account name
        LightToggleSwitch sharedIssuerSwitch = view.findViewById(R.id.pref_shared_issuer_account_name);
        sharedIssuerSwitch.setCheckedSilently(_prefs.onlyShowNecessaryAccountNames());
        sharedIssuerSwitch.setOnCheckedChangeListener(checked ->
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                    .edit().putBoolean("pref_shared_issuer_account_name", checked).apply()
        );
    }

    private void refreshAccountNamePositionText() {
        if (_accountNamePositionView == null) return;
        boolean override = (_prefs.getCurrentViewMode() == ViewMode.TILES
                && _prefs.getAccountNamePosition() == AccountNamePosition.END);
        String[] titles = getResources().getStringArray(R.array.account_name_position_titles);
        if (override) {
            _accountNamePositionView.setValue(titles[_prefs.getAccountNamePosition().ordinal()]
                    + ". " + getString(R.string.pref_account_name_position_summary_override));
        } else {
            _accountNamePositionView.setValue(titles[_prefs.getAccountNamePosition().ordinal()]);
        }
    }
}
