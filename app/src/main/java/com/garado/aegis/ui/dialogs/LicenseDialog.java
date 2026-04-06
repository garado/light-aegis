package com.garado.aegis.ui.dialogs;

import android.content.Context;

import com.garado.aegis.R;

public class LicenseDialog extends SimpleWebViewDialog {
    public LicenseDialog() {
        super(R.string.license);
    }

    public static LicenseDialog create() {
        return new LicenseDialog();
    }

    @Override
    protected String getContent(Context context) {
        String license = readAssetAsString(context, "LICENSE");
        String html = readAssetAsString(context, "license.html");
        return String.format(html, license, getBackgroundColor(), getTextColor());
    }
}
