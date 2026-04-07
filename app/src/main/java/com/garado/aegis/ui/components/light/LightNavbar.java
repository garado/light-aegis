package com.garado.aegis.ui.components.light;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.annotation.DrawableRes;
import com.google.android.material.color.MaterialColors;
import java.util.ArrayList;
import java.util.List;

/**
 * Bottom navigation bar matching the Light OS Navbar component.
 *
 * Active tab icon is full-color; inactive tabs are dimmed.
 * Tabs are added programmatically via addTab().
 *
 * Usage:
 *   navbar.addTab("home", R.drawable.ic_home_24);
 *   navbar.addTab("settings", R.drawable.ic_settings_24);
 *   navbar.setCurrentTab("home");
 *   navbar.setOnTabSelectedListener((name) -> { ... });
 */
public class LightNavbar extends LinearLayout {

    private final List<Tab> tabs = new ArrayList<>();
    private String currentTab = null;
    private OnTabSelectedListener listener;

    private final int activeColor;
    private final int inactiveColor;

    public interface OnTabSelectedListener {
        void onTabSelected(String name);
    }

    public LightNavbar(Context context) {
        this(context, null);
    }

    public LightNavbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LightNavbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        activeColor = MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnBackground, 0xFFFFFFFF);
        // 43% opacity for inactive (~#6E6E6E equivalent feel on monochrome)
        inactiveColor = (activeColor & 0x00FFFFFF) | 0x6E000000;

        int padding = dp(16);
        setPadding(padding, dp(10), padding, dp(10));
    }

    public void addTab(String name, @DrawableRes int iconRes) {
        Context context = getContext();

        ImageButton btn = new ImageButton(context);
        btn.setBackground(null);
        btn.setImageResource(iconRes);
        btn.setColorFilter(tabs.isEmpty() && currentTab == null ? activeColor : inactiveColor);

        int iconSize = dp(36);
        btn.setPadding(dp(8), dp(8), dp(8), dp(8));
        btn.setOnClickListener(v -> {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            setCurrentTab(name);
            if (listener != null) listener.onTabSelected(name);
        });

        LayoutParams params = new LayoutParams(0, iconSize, 1f);
        addView(btn, params);

        tabs.add(new Tab(name, btn));

        if (currentTab == null) setCurrentTab(name);
    }

    public void setCurrentTab(String name) {
        currentTab = name;
        for (Tab tab : tabs) {
            tab.button.setColorFilter(tab.name.equals(name) ? activeColor : inactiveColor);
        }
    }

    public void setOnTabSelectedListener(OnTabSelectedListener listener) {
        this.listener = listener;
    }

    private int dp(int value) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics()));
    }

    private static class Tab {
        final String name;
        final ImageButton button;
        Tab(String name, ImageButton button) {
            this.name = name;
            this.button = button;
        }
    }
}
