package com.garado.aegis.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.garado.aegis.R;
import com.garado.aegis.SortCategory;
import com.garado.aegis.helpers.ViewHelper;
import com.garado.aegis.ui.components.light.LightOptionGroup;

public class SortEntryActivity extends AegisActivity {

    public static final String EXTRA_SORT_CATEGORY = "sortCategory";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sort_entry);
        setSupportActionBar(findViewById(R.id.toolbar));
        ViewHelper.setupAppBarInsets(findViewById(R.id.app_bar_layout));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            setTitle(R.string.sort);
        }

        LightOptionGroup group = findViewById(R.id.sort_option_group);

        // Map child index → SortCategory (matches XML order)
        SortCategory[] order = {
            SortCategory.CUSTOM,
            SortCategory.ISSUER,
            SortCategory.ISSUER_REVERSED,
            SortCategory.ACCOUNT,
            SortCategory.ACCOUNT_REVERSED,
            SortCategory.USAGE_COUNT,
            SortCategory.LAST_USED,
        };

        // Pre-select current sort
        int currentOrdinal = getIntent().getIntExtra(EXTRA_SORT_CATEGORY, SortCategory.CUSTOM.ordinal());
        SortCategory current = SortCategory.fromInteger(currentOrdinal);
        for (int i = 0; i < order.length; i++) {
            if (order[i] == current) {
                group.select(i);
                break;
            }
        }

        group.setOnSelectionChangedListener((index, view) -> {
            Intent result = new Intent();
            result.putExtra(EXTRA_SORT_CATEGORY, order[index].ordinal());
            setResult(RESULT_OK, result);
            finish();
        });
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
