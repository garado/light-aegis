package com.garado.aegis.ui.fragments.preferences;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garado.aegis.BackupsVersioningStrategy;
import com.garado.aegis.Preferences;
import com.garado.aegis.R;
import com.garado.aegis.database.AuditLogRepository;
import com.garado.aegis.ui.components.light.LightButton;
import com.garado.aegis.ui.components.light.LightSelectorButton;
import com.garado.aegis.ui.components.light.LightText;
import com.garado.aegis.ui.components.light.LightToggleSwitch;
import com.garado.aegis.ui.dialogs.Dialogs;
import com.garado.aegis.vault.VaultBackupManager;
import com.garado.aegis.vault.VaultManager;
import com.garado.aegis.vault.VaultRepositoryException;
import com.google.android.material.color.MaterialColors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BackupsPreferencesFragment extends Fragment {

    @Inject Preferences _prefs;
    @Inject VaultManager _vaultManager;
    @Inject AuditLogRepository _auditLogRepository;

    private LightText _backupsPasswordWarningView;
    private LightToggleSwitch _androidBackupsPreference;
    private LightToggleSwitch _backupsPreference;
    private LightToggleSwitch _backupReminderPreference;
    private LightSelectorButton _versioningStrategyPreference;
    private LightSelectorButton _backupsLocationPreference;
    private LightButton _backupsTriggerPreference;
    private LightSelectorButton _backupsVersionsPreference;
    private LightSelectorButton _builtinBackupStatusPreference;
    private LightSelectorButton _androidBackupStatusPreference;

    protected boolean saveAndBackupVault() {
        try {
            _vaultManager.saveAndBackup();
        } catch (VaultRepositoryException e) {
            e.printStackTrace();
            Dialogs.showErrorDialog(requireContext(), R.string.saving_error, e);
            return false;
        }
        return true;
    }

    private final ActivityResultLauncher<Intent> backupsResultLauncher =
            registerForActivityResult(new StartActivityForResult(), activityResult -> {
                Intent data = activityResult.getData();
                int resultCode = activityResult.getResultCode();
                if (data != null) {
                    onSelectBackupsLocationResult(resultCode, data);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_preferences_backups, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        _backupsPasswordWarningView = view.findViewById(R.id.pref_backups_warning_password);
        _builtinBackupStatusPreference = view.findViewById(R.id.pref_status_backup_builtin);
        _builtinBackupStatusPreference.setOnClickListener(v -> {
            Preferences.BackupResult backupRes = _prefs.getBuiltInBackupResult();
            if (backupRes != null && !backupRes.isSuccessful()) {
                Dialogs.showBackupErrorDialog(requireContext(), backupRes, null);
            }
        });

        _androidBackupStatusPreference = view.findViewById(R.id.pref_status_backup_android);
        _androidBackupStatusPreference.setOnClickListener(v -> {
            Preferences.BackupResult backupRes = _prefs.getAndroidBackupResult();
            if (backupRes != null && !backupRes.isSuccessful()) {
                Dialogs.showBackupErrorDialog(requireContext(), backupRes, null);
            }
        });

        _backupsPreference = view.findViewById(R.id.pref_backups);
        _backupsPreference.setOnCheckedChangeListener(checked -> {
            _backupsPreference.setCheckedSilently(!checked);
            if (checked) {
                Dialogs.showBackupsVersioningStrategy(requireContext(), BackupsVersioningStrategy.MULTIPLE_BACKUPS, strategy -> {
                    if (strategy == BackupsVersioningStrategy.MULTIPLE_BACKUPS) selectBackupsLocation();
                    else if (strategy == BackupsVersioningStrategy.SINGLE_BACKUP) createBackupFile();
                });
            } else {
                _prefs.setIsBackupsEnabled(false);
                updateBackupPreference();
            }
        });

        _backupReminderPreference = view.findViewById(R.id.pref_backup_reminder);
        _backupReminderPreference.setOnCheckedChangeListener(checked -> {
            _backupReminderPreference.setCheckedSilently(!checked);
            if (!checked) {
                Dialogs.showCheckboxDialog(getContext(), R.string.pref_backups_reminder_dialog_title,
                        R.string.pref_backups_reminder_dialog_summary,
                        R.string.understand_risk_accept,
                        this::saveAndDisableBackupReminder
                );
            } else {
                _prefs.setIsBackupReminderEnabled(true);
                _backupReminderPreference.setCheckedSilently(true);
            }
        });

        _versioningStrategyPreference = view.findViewById(R.id.pref_versioning_strategy);
        updateBackupsVersioningStrategySummary();
        _versioningStrategyPreference.setOnClickListener(v -> {
            BackupsVersioningStrategy currentStrategy = _prefs.getBackupVersioningStrategy();
            Dialogs.showBackupsVersioningStrategy(requireContext(), currentStrategy, strategy -> {
                if (strategy == currentStrategy) return;
                if (strategy == BackupsVersioningStrategy.MULTIPLE_BACKUPS) selectBackupsLocation();
                else if (strategy == BackupsVersioningStrategy.SINGLE_BACKUP) createBackupFile();
            });
        });

        _androidBackupsPreference = view.findViewById(R.id.pref_android_backups);
        _androidBackupsPreference.setOnCheckedChangeListener(checked -> {
            _prefs.setIsAndroidBackupsEnabled(checked);
            updateBackupPreference();
            if (checked) _vaultManager.scheduleAndroidBackup();
        });

        _backupsLocationPreference = view.findViewById(R.id.pref_backups_location);
        updateBackupsLocationSummary();
        _backupsLocationPreference.setOnClickListener(v -> {
            BackupsVersioningStrategy currentStrategy = _prefs.getBackupVersioningStrategy();
            if (currentStrategy == BackupsVersioningStrategy.MULTIPLE_BACKUPS) selectBackupsLocation();
            else if (currentStrategy == BackupsVersioningStrategy.SINGLE_BACKUP) createBackupFile();
        });

        _backupsTriggerPreference = view.findViewById(R.id.pref_backups_trigger);
        _backupsTriggerPreference.setOnClickListener(v -> {
            if (_prefs.isBackupsEnabled()) {
                scheduleBackup();
                _builtinBackupStatusPreference.setVisibility(View.GONE);
            }
        });

        _backupsVersionsPreference = view.findViewById(R.id.pref_backups_versions);
        updateBackupsVersionsSummary();
        _backupsVersionsPreference.setOnClickListener(v ->
            Dialogs.showBackupVersionsPickerDialog(requireContext(), _prefs.getBackupsVersionCount(), number -> {
                _prefs.setBackupsVersionCount(number);
                updateBackupsVersionsSummary();
            })
        );

        updateBackupPreference();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (_backupsPreference != null) {
            updateBackupPreference();
        }
    }

    private void saveAndDisableBackupReminder(boolean understand) {
        if (understand) {
            _prefs.setIsBackupReminderEnabled(false);
            updateBackupPreference();
        }
    }

    private void onSelectBackupsLocationResult(int resultCode, Intent data) {
        Uri uri = data.getData();
        if (resultCode != Activity.RESULT_OK || uri == null) return;

        int flags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
        requireContext().getContentResolver().takePersistableUriPermission(data.getData(), flags);

        _prefs.setBackupsLocation(uri);
        _prefs.setIsBackupsEnabled(true);
        updateBackupPreference();
        scheduleBackup();
        updateBackupsVersioningStrategySummary();
        updateBackupsLocationSummary();
    }

    private void updateBackupPreference() {
        boolean encrypted = _vaultManager.getVault().isEncryptionEnabled();
        boolean androidBackupEnabled = _prefs.isAndroidBackupsEnabled() && encrypted;
        boolean backupEnabled = _prefs.isBackupsEnabled() && encrypted;
        boolean backupReminderEnabled = _prefs.isBackupReminderEnabled();

        _backupsPasswordWarningView.setVisibility(_vaultManager.getVault().isBackupPasswordSet() ? View.VISIBLE : View.GONE);
        _androidBackupsPreference.setCheckedSilently(androidBackupEnabled);
        _androidBackupsPreference.setEnabled(encrypted);
        _backupsPreference.setCheckedSilently(backupEnabled);
        _backupsPreference.setEnabled(encrypted);
        _backupReminderPreference.setCheckedSilently(backupReminderEnabled);

        int backupVisibility = backupEnabled ? View.VISIBLE : View.GONE;
        _versioningStrategyPreference.setVisibility(backupVisibility);
        _backupsLocationPreference.setVisibility(backupVisibility);
        _backupsTriggerPreference.setVisibility(backupVisibility);
        _backupsVersionsPreference.setVisibility(backupEnabled && _prefs.getBackupVersioningStrategy() != BackupsVersioningStrategy.SINGLE_BACKUP ? View.VISIBLE : View.GONE);

        if (backupEnabled) {
            updateBackupStatus(_builtinBackupStatusPreference, _prefs.getBuiltInBackupResult());
        }
        if (androidBackupEnabled) {
            updateBackupStatus(_androidBackupStatusPreference, _prefs.getAndroidBackupResult());
        }
        _builtinBackupStatusPreference.setVisibility(backupEnabled ? View.VISIBLE : View.GONE);
        _androidBackupStatusPreference.setVisibility(androidBackupEnabled ? View.VISIBLE : View.GONE);
    }

    private void updateBackupStatus(LightSelectorButton view, Preferences.BackupResult res) {
        boolean backupFailed = res != null && !res.isSuccessful();
        view.setValue(getBackupStatusMessage(res).toString());
        view.setClickable(backupFailed);
    }

    private CharSequence getBackupStatusMessage(@Nullable Preferences.BackupResult res) {
        String message;
        int colorAttr = com.google.android.material.R.attr.colorError;
        if (res == null) {
            message = getString(R.string.backup_status_none);
        } else if (res.isSuccessful()) {
            colorAttr = R.attr.colorSuccess;
            message = getString(R.string.backup_status_success, res.getElapsedSince(requireContext()));
        } else {
            message = getString(R.string.backup_status_failed, res.getElapsedSince(requireContext()));
        }

        int color = MaterialColors.getColor(requireContext(), colorAttr, getClass().getCanonicalName());
        Spannable spannable = new SpannableString(message);
        spannable.setSpan(new ForegroundColorSpan(color), 0, message.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, message.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    private void createBackupFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("application/json")
                .putExtra(Intent.EXTRA_TITLE, VaultBackupManager.FILENAME_SINGLE);
        _vaultManager.fireIntentLauncher(this, intent, backupsResultLauncher);
    }

    private void selectBackupsLocation() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        _vaultManager.fireIntentLauncher(this, intent, backupsResultLauncher);
    }

    private void scheduleBackup() {
        try {
            _vaultManager.scheduleBackup();
            Toast.makeText(requireContext(), R.string.backup_successful, Toast.LENGTH_LONG).show();
        } catch (VaultRepositoryException e) {
            e.printStackTrace();
            Dialogs.showErrorDialog(requireContext(), R.string.backup_error, e);
        }
    }

    private void updateBackupsVersioningStrategySummary() {
        BackupsVersioningStrategy currentStrategy = _prefs.getBackupVersioningStrategy();
        if (currentStrategy == BackupsVersioningStrategy.MULTIPLE_BACKUPS) {
            _versioningStrategyPreference.setValue(getString(R.string.pref_backups_versioning_strategy_keep_x_versions));
        } else if (currentStrategy == BackupsVersioningStrategy.SINGLE_BACKUP) {
            _versioningStrategyPreference.setValue(getString(R.string.pref_backups_versioning_strategy_single_backup));
        }
    }

    private void updateBackupsLocationSummary() {
        Uri backupsLocation = _prefs.getBackupsLocation();
        BackupsVersioningStrategy currentStrategy = _prefs.getBackupVersioningStrategy();
        String text;
        if (currentStrategy == BackupsVersioningStrategy.MULTIPLE_BACKUPS) {
            text = getString(R.string.pref_backups_location_summary);
        } else if (currentStrategy == BackupsVersioningStrategy.SINGLE_BACKUP) {
            text = getString(R.string.pref_backup_location_summary);
        } else {
            return;
        }
        _backupsLocationPreference.setValue(String.format("%s: %s", text, Uri.decode(backupsLocation.toString())));
    }

    private void updateBackupsVersionsSummary() {
        int count = _prefs.getBackupsVersionCount();
        if (count == Preferences.BACKUPS_VERSIONS_INFINITE) {
            _backupsVersionsPreference.setValue(getString(R.string.pref_backups_versions_infinite_summary));
        } else {
            _backupsVersionsPreference.setValue(getResources().getQuantityString(R.plurals.pref_backups_versions_summary, count, count));
        }
    }
}
