package com.garado.aegis.ui.components.light;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.widget.LinearLayout;
import com.garado.aegis.R;

/**
 * A tappable selector row showing a small label above a large value.
 * Matches the Light OS SelectorButton component.
 *
 * Attrs: app:lightLabel, app:lightValue
 */
public class LightSelectorButton extends LinearLayout {

    private final LightText labelView;
    private final LightText valueView;
    private OnClickListener onClickListener;
    private boolean selectable = true;

    public LightSelectorButton(Context context) {
        this(context, null);
    }

    public LightSelectorButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LightSelectorButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setOrientation(VERTICAL);
        setClickable(true);
        setFocusable(true);
        setBackground(null);

        labelView = new LightText(context);
        labelView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        labelView.setSingleLine(true);
        labelView.setEllipsize(android.text.TextUtils.TruncateAt.END);
        addView(labelView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        valueView = new LightText(context);
        valueView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        LayoutParams valueParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        valueParams.bottomMargin = dp(8);
        addView(valueView, valueParams);

        setOnClickListener(v -> {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            if (onClickListener != null) onClickListener.onClick(v);
        });

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LightSelectorButton);
            String label = a.getString(R.styleable.LightSelectorButton_lightLabel);
            if (label != null) setLabel(label);
            String value = a.getString(R.styleable.LightSelectorButton_lightValue);
            if (value != null) setValue(value);
            selectable = a.getBoolean(R.styleable.LightSelectorButton_lightSelectable, true);
            a.recycle();
        }
    }

    public void setLabel(String label) {
        labelView.setText(label);
    }

    public void setValue(String value) {
        valueView.setText(value);
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
        if (!selectable) setSelected(false);
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        boolean underline = selected && selectable;
        if (underline) {
            valueView.setPaintFlags(valueView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        } else {
            valueView.setPaintFlags(valueView.getPaintFlags() & ~Paint.UNDERLINE_TEXT_FLAG);
        }
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        this.onClickListener = listener;
        super.setOnClickListener(listener);
    }

    private int dp(int value) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics()));
    }
}
