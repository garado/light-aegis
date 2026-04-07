package com.garado.aegis.ui.components.light;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.res.ResourcesCompat;
import com.garado.aegis.R;

public class LightText extends AppCompatTextView {

    public LightText(Context context) {
        super(context);
        init(context);
    }

    public LightText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LightText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        int fgColor = com.google.android.material.color.MaterialColors.getColor(
                context, com.google.android.material.R.attr.colorOnBackground, 0xFF000000);
        setTextColor(fgColor);
        setLineSpacing(0f, 1.2f);
        Typeface typeface = ResourcesCompat.getFont(context, R.font.public_sans_regular);
        if (typeface != null) setTypeface(typeface);
    }
}
