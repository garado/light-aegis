package com.garado.aegis.ui.fragments.preferences;

import static android.text.TextUtils.isDigitsOnly;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.garado.aegis.PassReminderFreq;
import com.garado.aegis.Preferences;
import com.garado.aegis.R;
import com.garado.aegis.crypto.KeyStoreHandle;
import com.garado.aegis.crypto.KeyStoreHandleException;
import com.garado.aegis.database.AuditLogRepository;
import com.garado.aegis.helpers.BiometricSlotInitializer;
import com.garado.aegis.helpers.BiometricsHelper;
import com.garado.aegis.ui.components.light.LightButton;
import com.garado.aegis.ui.components.light.LightSelectorButton;
import com.garado.aegis.ui.components.light.LightToggleSwitch;
import com.garado.aegis.ui.dialogs.Dialogs;
import com.garado.aegis.ui.tasks.PasswordSlotDecryptTask;
import com.garado.aegis.vault.VaultFileCredentials;
import com.garado.aegis.vault.VaultManager;
import com.garado.aegis.vault.VaultRepositoryException;
import com.garado.aegis.vault.slots.BiometricSlot;
import com.garado.aegis.vault.slots.PasswordSlot;
import com.garado.aegis.vault.slots.Slot;
import com.garado.aegis.vault.slots.SlotException;
import com.garado.aegis.vault.slots.SlotList;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;
import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SecurityPreferencesFragment extends Fragment {

    @Inject Preferences _prefs;
    @Inject VaultManager _vaultManager;
    @Inject AuditLogRepository _auditLogRepository;

    private LightToggleSwitch _encryptionPreference;
    private LightToggleSwitch _biometricsPreference;
    private LightSelectorButton _autoLockPreference;
    private LightButton _setPasswordPreference;
    private LightSelectorButton _passwordReminderPreference;
    private LightToggleSwitch _pinKeyboardPreference;
    private LightToggleSwitch _backupPasswordPreference;
    private LightButton _backupPasswordChangePreference;
    private View _groupBackupPassword;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_preferences_security, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        _encryptionPreference = view.findViewById(R.id.pref_encryption);
        _setPasswordPreference = view.findViewById(R.id.pref_password);
        _biometricsPreference = view.findViewById(R.id.pref_biometrics);
        _autoLockPreference = view.findViewById(R.id.pref_auto_lock);
        _pinKeyboardPreference = view.findViewById(R.id.pref_pin_keyboard);
        _passwordReminderPreference = view.findViewById(R.id.pref_password_reminder_freq);
        _groupBackupPassword = view.findViewById(R.id.group_backup_password);
        _backupPasswordPreference = view.findViewById(R.id.pref_backup_password);
        _backupPasswordChangePreference = view.findViewById(R.id.pref_backup_password_change);

        // Secure screen
        LightToggleSwitch secureScreenSwitch = view.findViewById(R.id.pref_secure_screen);
        secureScreenSwitch.setCheckedSilently(_prefs.isSecureScreenEnabled());
        secureScreenSwitch.setOnCheckedChangeListener(checked -> {
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                    .edit().putBoolean("pref_secure_screen", checked).apply();
            Window window = requireActivity().getWindow();
            if (checked) window.addFlags(WindowManager.LayoutParams.FLAG_SECURE);
            else window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        });

        // Tap to reveal time
        LightSelectorButton tapToRevealTimeBtn = view.findViewById(R.id.pref_tap_to_reveal_time);
        tapToRevealTimeBtn.setValue(_prefs.getTapToRevealTime() + " seconds");
        tapToRevealTimeBtn.setOnClickListener(v ->
            Dialogs.showTapToRevealTimeoutPickerDialog(requireContext(), _prefs.getTapToRevealTime(), number -> {
                _prefs.setTapToRevealTime(number);
                tapToRevealTimeBtn.setValue(number + " seconds");
            })
        );

        // Tap to reveal
        LightToggleSwitch tapToRevealSwitch = view.findViewById(R.id.pref_tap_to_reveal);
        tapToRevealSwitch.setCheckedSilently(_prefs.isTapToRevealEnabled());
        tapToRevealSwitch.setOnCheckedChangeListener(checked ->
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                    .edit().putBoolean("pref_tap_to_reveal", checked).apply()
        );

        // Panic trigger
        LightToggleSwitch panicTriggerSwitch = view.findViewById(R.id.pref_panic_trigger);
        panicTriggerSwitch.setCheckedSilently(_prefs.isPanicTriggerEnabled());
        panicTriggerSwitch.setOnCheckedChangeListener(checked ->
                _prefs.setIsPanicTriggerEnabled(checked));

        // Encryption toggle
        _encryptionPreference.setOnCheckedChangeListener(checked -> {
            // ignore - actual logic handled via dialog
            _encryptionPreference.setCheckedSilently(!checked);
            if (!_vaultManager.getVault().isEncryptionEnabled()) {
                Dialogs.showSetPasswordDialog(requireActivity(), new EnableEncryptionListener());
            } else {
                Dialogs.showSecureDialog(new MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_Aegis_AlertDialog_Warning)
                        .setTitle(R.string.disable_encryption)
                        .setMessage(getText(R.string.disable_encryption_description))
                        .setIconAttribute(android.R.attr.alertDialogIcon)
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            try {
                                _vaultManager.disableEncryption();
                            } catch (VaultRepositoryException e) {
                                e.printStackTrace();
                                Dialogs.showErrorDialog(requireContext(), R.string.disable_encryption_error, e);
                                return;
                            }
                            _prefs.setIsBackupsEnabled(false);
                            _prefs.setIsAndroidBackupsEnabled(false);
                            updateEncryptionPreferences();
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .create());
            }
        });

        // Biometrics toggle
        _biometricsPreference.setOnCheckedChangeListener(checked -> {
            _biometricsPreference.setCheckedSilently(!checked);
            VaultFileCredentials creds = _vaultManager.getVault().getCredentials();
            SlotList slots = creds.getSlots();
            if (!slots.has(BiometricSlot.class)) {
                if (BiometricsHelper.isAvailable(requireContext())) {
                    BiometricSlotInitializer initializer = new BiometricSlotInitializer(SecurityPreferencesFragment.this, new RegisterBiometricsListener());
                    BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
                            .setTitle(getString(R.string.set_up_biometric))
                            .setNegativeButtonText(getString(android.R.string.cancel))
                            .build();
                    initializer.authenticate(info);
                }
            } else {
                BiometricSlot slot = slots.find(BiometricSlot.class);
                slots.remove(slot);
                _vaultManager.getVault().setCredentials(creds);
                try {
                    KeyStoreHandle handle = new KeyStoreHandle();
                    handle.deleteKey(slot.getUUID().toString());
                } catch (KeyStoreHandleException e) {
                    e.printStackTrace();
                }
                saveAndBackupVault();
                updateEncryptionPreferences();
            }
        });

        // Set password button
        _setPasswordPreference.setOnClickListener(v ->
                Dialogs.showSetPasswordDialog(requireActivity(), new SetPasswordListener()));

        // Pin keyboard toggle
        _pinKeyboardPreference.setCheckedSilently(_prefs.isPinKeyboardEnabled());
        _pinKeyboardPreference.setOnCheckedChangeListener(checked -> {
            if (!checked) {
                PreferenceManager.getDefaultSharedPreferences(requireContext())
                        .edit().putBoolean("pref_pin_keyboard", false).apply();
                return;
            }
            _pinKeyboardPreference.setCheckedSilently(false);
            Dialogs.showPasswordInputDialog(requireContext(), R.string.set_password_confirm, R.string.pin_keyboard_description, password -> {
                if (isDigitsOnly(new String(password))) {
                    List<PasswordSlot> slots = _vaultManager.getVault().getCredentials().getSlots().findRegularPasswordSlots();
                    PasswordSlotDecryptTask.Params params = new PasswordSlotDecryptTask.Params(slots, password);
                    PasswordSlotDecryptTask task = new PasswordSlotDecryptTask(requireContext(), new PasswordConfirmationListener());
                    task.execute(getLifecycle(), params);
                } else {
                    _pinKeyboardPreference.setCheckedSilently(false);
                    Dialogs.showSecureDialog(new MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_Aegis_AlertDialog_Error)
                            .setTitle(R.string.pin_keyboard_error)
                            .setMessage(R.string.pin_keyboard_error_description)
                            .setIconAttribute(android.R.attr.alertDialogIcon)
                            .setCancelable(false)
                            .setPositiveButton(android.R.string.ok, null)
                            .create());
                }
            }, dialog -> _pinKeyboardPreference.setCheckedSilently(false));
        });

        // Auto lock selector
        _autoLockPreference.setValue(getAutoLockSummary());
        _autoLockPreference.setOnClickListener(v -> {
            final int[] items = Preferences.AUTO_LOCK_SETTINGS;
            final String[] textItems = getResources().getStringArray(R.array.pref_auto_lock_types);
            final boolean[] checkedItems = new boolean[items.length];
            for (int i = 0; i < items.length; i++) {
                checkedItems[i] = _prefs.isAutoLockTypeEnabled(items[i]);
            }
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.pref_auto_lock_prompt)
                    .setMultiChoiceItems(textItems, checkedItems, (dialog, index, isChecked) -> checkedItems[index] = isChecked)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        int autoLock = Preferences.AUTO_LOCK_OFF;
                        for (int i = 0; i < checkedItems.length; i++) {
                            if (checkedItems[i]) autoLock |= items[i];
                        }
                        _prefs.setAutoLockMask(autoLock);
                        _autoLockPreference.setValue(getAutoLockSummary());
                    })
                    .setNegativeButton(android.R.string.cancel, null);
            Dialogs.showSecureDialog(builder.create());
        });

        // Password reminder frequency selector
        _passwordReminderPreference.setValue(getPasswordReminderSummary());
        _passwordReminderPreference.setOnClickListener(v -> {
            final PassReminderFreq currFreq = _prefs.getPasswordReminderFrequency();
            final PassReminderFreq[] items = PassReminderFreq.values();
            final String[] textItems = Arrays.stream(items)
                    .map(f -> getString(f.getStringRes()))
                    .toArray(String[]::new);
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.pref_password_reminder_title)
                    .setSingleChoiceItems(textItems, currFreq.ordinal(), (dialog, which) -> {
                        int i = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        PassReminderFreq freq = PassReminderFreq.fromInteger(i);
                        _prefs.setPasswordReminderFrequency(freq);
                        _passwordReminderPreference.setValue(getPasswordReminderSummary());
                        dialog.dismiss();
                    })
                    .setNegativeButton(android.R.string.cancel, null);
            Dialogs.showSecureDialog(builder.create());
        });

        // Backup password toggle
        _backupPasswordPreference.setOnCheckedChangeListener(checked -> {
            _backupPasswordPreference.setCheckedSilently(!checked);
            if (!_vaultManager.getVault().isBackupPasswordSet()) {
                Dialogs.showSetPasswordDialog(requireActivity(), new SetBackupPasswordListener());
            } else {
                VaultFileCredentials creds = _vaultManager.getVault().getCredentials();
                SlotList slots = creds.getSlots();
                for (Slot slot : slots.findBackupPasswordSlots()) {
                    slots.remove(slot);
                }
                _vaultManager.getVault().setCredentials(creds);
                saveAndBackupVault();
                updateEncryptionPreferences();
            }
        });

        // Backup password change button
        _backupPasswordChangePreference.setOnClickListener(v ->
                Dialogs.showSetPasswordDialog(requireActivity(), new SetBackupPasswordListener()));

        updateEncryptionPreferences();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (_encryptionPreference != null) {
            updateEncryptionPreferences();
        }
    }

    private void updateEncryptionPreferences() {
        boolean encrypted = _vaultManager.getVault().isEncryptionEnabled();
        boolean backupPasswordSet = _vaultManager.getVault().isBackupPasswordSet();
        _encryptionPreference.setCheckedSilently(encrypted);
        int encVisibility = encrypted ? View.VISIBLE : View.GONE;
        _setPasswordPreference.setVisibility(encVisibility);
        _biometricsPreference.setVisibility(encVisibility);
        _autoLockPreference.setVisibility(encVisibility);
        _pinKeyboardPreference.setVisibility(encVisibility);
        _groupBackupPassword.setVisibility(encVisibility);
        _backupPasswordPreference.setCheckedSilently(backupPasswordSet);
        _backupPasswordChangePreference.setVisibility(backupPasswordSet ? View.VISIBLE : View.GONE);

        if (encrypted) {
            SlotList slots = _vaultManager.getVault().getCredentials().getSlots();
            boolean multiBackupPassword = slots.findBackupPasswordSlots().size() > 1;
            boolean multiPassword = slots.findRegularPasswordSlots().size() > 1;
            boolean multiBio = slots.findAll(BiometricSlot.class).size() > 1;
            boolean canUseBio = BiometricsHelper.isAvailable(requireContext());
            _setPasswordPreference.setEnabled(!multiPassword);
            _biometricsPreference.setEnabled(canUseBio && !multiBio);
            _biometricsPreference.setCheckedSilently(slots.has(BiometricSlot.class));
            _passwordReminderPreference.setVisibility(slots.has(BiometricSlot.class) ? View.VISIBLE : View.GONE);
            _backupPasswordChangePreference.setEnabled(!multiBackupPassword);
        } else {
            _setPasswordPreference.setEnabled(false);
            _biometricsPreference.setEnabled(false);
            _biometricsPreference.setCheckedSilently(false);
            _passwordReminderPreference.setVisibility(View.GONE);
            _backupPasswordChangePreference.setEnabled(false);
        }
    }

    private String getPasswordReminderSummary() {
        PassReminderFreq freq = _prefs.getPasswordReminderFrequency();
        if (freq == PassReminderFreq.NEVER) {
            return getString(R.string.pref_password_reminder_summary_disabled);
        }
        String freqString = getString(freq.getStringRes()).toLowerCase();
        return getString(R.string.pref_password_reminder_summary, freqString);
    }

    private String getAutoLockSummary() {
        final int[] settings = Preferences.AUTO_LOCK_SETTINGS;
        final String[] descriptions = getResources().getStringArray(R.array.pref_auto_lock_types);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < settings.length; i++) {
            if (_prefs.isAutoLockTypeEnabled(settings[i])) {
                if (builder.length() != 0) builder.append(", ");
                builder.append(descriptions[i].toLowerCase());
            }
        }
        if (builder.length() == 0) return getString(R.string.pref_auto_lock_summary_disabled);
        return getString(R.string.pref_auto_lock_summary, builder.toString());
    }

    private class SetPasswordListener implements Dialogs.PasswordSlotListener {
        @Override
        public void onSlotResult(PasswordSlot slot, Cipher cipher) {
            VaultFileCredentials creds = _vaultManager.getVault().getCredentials();
            SlotList slots = creds.getSlots();
            try {
                slot.setKey(creds.getKey(), cipher);
                List<PasswordSlot> passSlots = creds.getSlots().findRegularPasswordSlots();
                if (passSlots.size() != 0) slots.remove(passSlots.get(0));
                slots.add(slot);
            } catch (SlotException e) {
                onException(e);
                return;
            }
            _vaultManager.getVault().setCredentials(creds);
            saveAndBackupVault();
            if (_prefs.isPinKeyboardEnabled()) {
                _pinKeyboardPreference.setCheckedSilently(false);
                Toast.makeText(requireContext(), R.string.pin_keyboard_disabled, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onException(Exception e) {
            e.printStackTrace();
            updateEncryptionPreferences();
            Dialogs.showErrorDialog(requireContext(), R.string.encryption_set_password_error, e);
        }
    }

    private class SetBackupPasswordListener implements Dialogs.PasswordSlotListener {
        @Override
        public void onSlotResult(PasswordSlot slot, Cipher cipher) {
            slot.setIsBackup(true);
            VaultFileCredentials creds = _vaultManager.getVault().getCredentials();
            SlotList slots = creds.getSlots();
            try {
                slot.setKey(creds.getKey(), cipher);
                for (Slot oldSlot : slots.findBackupPasswordSlots()) slots.remove(oldSlot);
                slots.add(slot);
            } catch (SlotException e) {
                onException(e);
                return;
            }
            _vaultManager.getVault().setCredentials(creds);
            saveAndBackupVault();
            updateEncryptionPreferences();
        }

        @Override
        public void onException(Exception e) {
            e.printStackTrace();
            updateEncryptionPreferences();
            Dialogs.showErrorDialog(requireContext(), R.string.encryption_set_password_error, e);
        }
    }

    private class RegisterBiometricsListener implements BiometricSlotInitializer.Listener {
        @Override
        public void onInitializeSlot(BiometricSlot slot, Cipher cipher) {
            VaultFileCredentials creds = _vaultManager.getVault().getCredentials();
            try {
                slot.setKey(creds.getKey(), cipher);
            } catch (SlotException e) {
                e.printStackTrace();
                onSlotInitializationFailed(0, e.toString());
                return;
            }
            creds.getSlots().add(slot);
            _vaultManager.getVault().setCredentials(creds);
            saveAndBackupVault();
            updateEncryptionPreferences();
        }

        @Override
        public void onSlotInitializationFailed(int errorCode, @NonNull CharSequence errString) {
            if (!BiometricsHelper.isCanceled(errorCode)) {
                Dialogs.showErrorDialog(requireContext(), R.string.encryption_enable_biometrics_error, errString);
            }
        }
    }

    private class EnableEncryptionListener implements Dialogs.PasswordSlotListener {
        @Override
        public void onSlotResult(PasswordSlot slot, Cipher cipher) {
            VaultFileCredentials creds = new VaultFileCredentials();
            try {
                slot.setKey(creds.getKey(), cipher);
                creds.getSlots().add(slot);
                _vaultManager.enableEncryption(creds);
            } catch (VaultRepositoryException | SlotException e) {
                onException(e);
                return;
            }
            _pinKeyboardPreference.setCheckedSilently(false);
            updateEncryptionPreferences();
        }

        @Override
        public void onException(Exception e) {
            e.printStackTrace();
            Dialogs.showErrorDialog(requireContext(), R.string.encryption_set_password_error, e);
        }
    }

    private class PasswordConfirmationListener implements PasswordSlotDecryptTask.Callback {
        @Override
        public void onTaskFinished(PasswordSlotDecryptTask.Result result) {
            if (result != null) {
                _pinKeyboardPreference.setCheckedSilently(true);
                PreferenceManager.getDefaultSharedPreferences(requireContext())
                        .edit().putBoolean("pref_pin_keyboard", true).apply();
            } else {
                Dialogs.showSecureDialog(new MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_Aegis_AlertDialog_Error)
                        .setTitle(R.string.pin_keyboard_error)
                        .setMessage(R.string.invalid_password)
                        .setIconAttribute(android.R.attr.alertDialogIcon)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok, null)
                        .create());
                _pinKeyboardPreference.setCheckedSilently(false);
            }
        }
    }
}
