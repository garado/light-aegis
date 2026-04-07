package com.garado.aegis.ui.components.light;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import com.garado.aegis.R;

/**
 * Button styled like "CONFIRM"
 * Supports android:text natively.
 *
 * Attrs: app:lightSelected
 */
public class LightConfirmButton extends LightText {

    public LightConfirmButton(Context context) {
        this(context, null);
    }

    public LightConfirmButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LightConfirmButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
        setSingleLine(true);
        setEllipsize(android.text.TextUtils.TruncateAt.END);
        setClickable(true);
        setFocusable(true);
        setAllCaps(true);
        setGravity(android.view.Gravity.BOTTOM | android.view.Gravity.CENTER);

        setOnClickListener(v -> performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY));

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LightConfirmButton);
            setSelected(a.getBoolean(R.styleable.LightConfirmButton_lightSelected, false));
            a.recycle();
        }
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        if (selected) {
            setPaintFlags(getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        } else {
            setPaintFlags(getPaintFlags() & ~Paint.UNDERLINE_TEXT_FLAG);
        }
    }
}
