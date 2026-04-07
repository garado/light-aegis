package com.garado.aegis.ui;

import android.os.Bundle;
import android.view.MenuItem;

import com.garado.aegis.R;
import com.garado.aegis.helpers.ViewHelper;

public class AddEntryActivity extends AegisActivity {

    public static final int RESULT_SCAN = 1;
    public static final int RESULT_SCAN_IMAGE = 2;
    public static final int RESULT_ENTER = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_entry);
        setSupportActionBar(findViewById(R.id.toolbar));
        ViewHelper.setupAppBarInsets(findViewById(R.id.app_bar_layout));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            setTitle(R.string.add_new_entry);
        }

        findViewById(R.id.btn_scan).setOnClickListener(v -> { setResult(RESULT_SCAN); finish(); });
        findViewById(R.id.btn_scan_image).setOnClickListener(v -> { setResult(RESULT_SCAN_IMAGE); finish(); });
        findViewById(R.id.btn_enter).setOnClickListener(v -> { setResult(RESULT_ENTER); finish(); });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
