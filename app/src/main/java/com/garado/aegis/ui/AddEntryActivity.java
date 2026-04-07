package com.garado.aegis.ui;

import android.app.Activity;
import android.os.Bundle;

import com.garado.aegis.R;
import com.garado.aegis.ui.components.light.LightHeader;

public class AddEntryActivity extends AegisActivity {

    public static final int RESULT_SCAN = 1;
    public static final int RESULT_SCAN_IMAGE = 2;
    public static final int RESULT_ENTER = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_entry);

        LightHeader header = findViewById(R.id.lightHeader);
        header.setOnBackPressedListener(this::finish);

        findViewById(R.id.btn_scan).setOnClickListener(v -> { setResult(RESULT_SCAN); finish(); });
        findViewById(R.id.btn_scan_image).setOnClickListener(v -> { setResult(RESULT_SCAN_IMAGE); finish(); });
        findViewById(R.id.btn_enter).setOnClickListener(v -> { setResult(RESULT_ENTER); finish(); });
    }
}
