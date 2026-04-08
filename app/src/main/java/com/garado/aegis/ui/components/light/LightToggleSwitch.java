package com.garado.aegis.ui.components.light;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.LinearLayout;
import com.garado.aegis.R;
import com.google.android.material.color.MaterialColors;

/**
 * Toggle switch with label matching the Light OS aesthetic.
 * Attrs: app:lightLabel, app:lightValue, app:lightChecked
 */
public class LightToggleSwitch extends LinearLayout {

    private final LightText labelView;
    private final ToggleGraphic graphic;
    private final LightText valueView;
    private boolean checked = false;
    private OnCheckedChangeListener listener;
    private OnClickListener _externalClickListener;

    public interface OnCheckedChangeListener {
        void onCheckedChanged(boolean checked);
    }

    public LightToggleSwitch(Context context) {
        this(context, null);
    }

    public LightToggleSwitch(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LightToggleSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setOrientation(VERTICAL);
        setClickable(true);
        setFocusable(true);

        int fgColor = MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnBackground, 0xFFFFFFFF);

        this.setOrientation(HORIZONTAL);
        this.setGravity(android.view.Gravity.CENTER_VERTICAL);
        
        graphic = new ToggleGraphic(context, fgColor);
        int graphicWidth = dp(context, 20);
        int graphicHeight = dp(context, 8);
        LinearLayout.LayoutParams graphicParams = new LayoutParams(graphicWidth, graphicHeight);
        graphicParams.gravity = android.view.Gravity.TOP;
        graphicParams.topMargin = dp(context, 15);
        graphicParams.rightMargin = dp(context, 16);
        addView(graphic, graphicParams);
        
        LinearLayout column = new LinearLayout(context);
        column.setOrientation(VERTICAL);
        LinearLayout.LayoutParams columnParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
        column.setLayoutParams(columnParams);
        
        valueView = new LightText(context);
        valueView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
        column.addView(valueView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        
        labelView = new LightText(context);
        labelView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        labelView.setVisibility(GONE);
        column.addView(labelView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        
        addView(column);

        super.setOnClickListener(v -> {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            setChecked(!checked);
            if (_externalClickListener != null) _externalClickListener.onClick(v);
        });

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LightToggleSwitch);
            String label = a.getString(R.styleable.LightToggleSwitch_lightLabel);
            if (label != null) setLabel(label);
            String value = a.getString(R.styleable.LightToggleSwitch_lightValue);
            if (value != null) setValue(value);
            setChecked(a.getBoolean(R.styleable.LightToggleSwitch_lightChecked, false));
            a.recycle();
        }
    }

    public void setLabel(String text) {
        labelView.setText(text);
        labelView.setVisibility(text != null && !text.isEmpty() ? VISIBLE : GONE);
    }

    public void setValue(String text) {
        valueView.setText(text);
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        graphic.setChecked(checked);
        if (listener != null) listener.onCheckedChanged(checked);
    }

    public void setCheckedSilently(boolean checked) {
        this.checked = checked;
        graphic.setChecked(checked);
    }

    public boolean isChecked() {
        return checked;
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        _externalClickListener = l;
    }

    private static int dp(Context context, int value) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, context.getResources().getDisplayMetrics()));
    }

    // -------------------------------------------------------------------------
    // Inner graphic view
    // -------------------------------------------------------------------------

    private static class ToggleGraphic extends View {

        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final int color;
        private boolean checked = false;

        ToggleGraphic(Context context, int color) {
            super(context);
            this.color = color;
            paint.setColor(color);
        }

        void setChecked(boolean checked) {
            this.checked = checked;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            float w = getWidth();
            float h = getHeight();
            float cx = h / 2f;           // circle diameter = height
            float r = cx;                // radius
            float strokeWidth = r * 0.4f;
            float lineH = h * 0.14f;     // line thickness
            float lineY = h / 2f - lineH / 2f;
            float lineW = w - cx * 2;    // line fills space beside circle

            paint.setStrokeWidth(strokeWidth);

            if (checked) {
                // line — filled circle
                paint.setStyle(Paint.Style.FILL);
                canvas.drawRect(0, lineY, lineW, lineY + lineH, paint);
                canvas.drawCircle(w - r, h / 2f, r, paint);
            } else {
                // hollow circle — line
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawCircle(r, h / 2f, r - strokeWidth / 2f, paint);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawRect(cx * 2, lineY, w, lineY + lineH, paint);
            }
        }
    }
}
