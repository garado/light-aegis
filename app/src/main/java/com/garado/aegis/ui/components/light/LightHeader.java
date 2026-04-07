package com.garado.aegis.ui.components.light;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.annotation.DrawableRes;
import com.garado.aegis.R;
import com.google.android.material.color.MaterialColors;

/**
 * Header bar matching the Light OS Header component.
 *
 * Layout: [back button] [title (centered, flex)] [right action button]
 * Both buttons are fixed-width so the title is truly centered.
 *
 * Attrs: app:lightTitle, app:lightHideBackButton
 */
public class LightHeader extends LinearLayout {

    private final ImageButton backButton;
    private final LightText titleView;
    private final ImageButton rightButton;

    private OnBackPressedListener backListener;
    private OnRightActionListener rightListener;

    public interface OnBackPressedListener {
        void onBackPressed();
    }

    public interface OnRightActionListener {
        void onRightAction();
    }

    public LightHeader(Context context) {
        this(context, null);
    }

    public LightHeader(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LightHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        int fgColor = MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnBackground, 0xFFFFFFFF);
        int buttonSize = dp(44);
        int iconPadding = dp(10);

        // Back button
        backButton = new ImageButton(context);
        backButton.setBackground(null);
        backButton.setImageResource(R.drawable.ic_outline_arrow_left_alt_24);
        backButton.setColorFilter(fgColor);
        backButton.setPadding(iconPadding, iconPadding, iconPadding, iconPadding);
        backButton.setOnClickListener(v -> {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            if (backListener != null) backListener.onBackPressed();
        });
        addView(backButton, new LayoutParams(buttonSize, buttonSize));

        // Title
        titleView = new LightText(context);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        titleView.setGravity(Gravity.CENTER);
        titleView.setSingleLine(true);
        titleView.setEllipsize(android.text.TextUtils.TruncateAt.END);
        LayoutParams titleParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
        addView(titleView, titleParams);

        // Right action button (hidden by default)
        rightButton = new ImageButton(context);
        rightButton.setBackground(null);
        rightButton.setColorFilter(fgColor);
        rightButton.setPadding(iconPadding, iconPadding, iconPadding, iconPadding);
        rightButton.setVisibility(INVISIBLE);
        rightButton.setOnClickListener(v -> {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            if (rightListener != null) rightListener.onRightAction();
        });
        addView(rightButton, new LayoutParams(buttonSize, buttonSize));

        int paddingH = dp(16);
        int paddingV = dp(8);
        setPadding(paddingH, paddingV, paddingH, paddingV);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LightHeader);
            String title = a.getString(R.styleable.LightHeader_lightTitle);
            if (title != null) setTitle(title);
            if (a.getBoolean(R.styleable.LightHeader_lightHideBackButton, false)) {
                backButton.setVisibility(INVISIBLE);
            }
            a.recycle();
        }
    }

    public void setTitle(String title) {
        titleView.setText(title);
    }

    public void setHideBackButton(boolean hide) {
        backButton.setVisibility(hide ? INVISIBLE : VISIBLE);
    }

    public void setRightAction(@DrawableRes int iconRes, OnRightActionListener listener) {
        rightButton.setImageResource(iconRes);
        rightButton.setVisibility(VISIBLE);
        this.rightListener = listener;
    }

    public void hideRightAction() {
        rightButton.setVisibility(INVISIBLE);
    }

    public void setOnBackPressedListener(OnBackPressedListener listener) {
        this.backListener = listener;
    }

    private int dp(int value) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics()));
    }
}
