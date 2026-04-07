package com.garado.aegis.ui.components.light;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import com.garado.aegis.R;
import com.google.android.material.color.MaterialColors;

/**
 * Text input with a bottom border only and an inline clear button.
 * Matches the Light OS TextInput component.
 *
 * Attrs: app:lightHint, app:lightAutoFocus
 */
public class LightTextInput extends LinearLayout {

    private final EditText editText;
    private final ImageButton clearButton;
    private OnChangeListener onChangeListener;
    private OnSubmitListener onSubmitListener;

    public interface OnChangeListener {
        void onTextChanged(String text);
    }

    public interface OnSubmitListener {
        void onSubmit();
    }

    public LightTextInput(Context context) {
        this(context, null);
    }

    public LightTextInput(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LightTextInput(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setOrientation(VERTICAL);

        int fgColor = MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnBackground, 0xFFFFFFFF);

        // Row: EditText + clear button
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(HORIZONTAL);

        editText = new EditText(context);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        editText.setTextColor(fgColor);
        editText.setHintTextColor(fgColor);
        editText.setCursorVisible(true);
        editText.setBackground(null);
        editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        editText.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH);
        Typeface typeface = ResourcesCompat.getFont(context, R.font.public_sans_regular);
        if (typeface != null) editText.setTypeface(typeface);

        LinearLayout.LayoutParams editParams = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
        row.addView(editText, editParams);

        clearButton = new ImageButton(context);
        clearButton.setImageDrawable(ContextCompat.getDrawable(context, android.R.drawable.ic_menu_close_clear_cancel));
        clearButton.setColorFilter(fgColor);
        clearButton.setBackground(null);
        clearButton.setVisibility(GONE);
        clearButton.setPadding(dp(8), dp(4), 0, dp(4));
        clearButton.setOnClickListener(v -> {
            editText.setText("");
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        });
        row.addView(clearButton, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        addView(row, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        // Bottom border line
        View border = new View(context);
        border.setBackgroundColor(fgColor);
        LayoutParams borderParams = new LayoutParams(LayoutParams.MATCH_PARENT, dp(1));
        addView(border, borderParams);

        // Wiring
        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                clearButton.setVisibility(s.length() > 0 ? VISIBLE : GONE);
                if (onChangeListener != null) onChangeListener.onTextChanged(s.toString());
            }
        });

        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (onSubmitListener != null) onSubmitListener.onSubmit();
            return true;
        });

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LightTextInput);
            String hint = a.getString(R.styleable.LightTextInput_lightHint);
            if (hint != null) editText.setHint(hint);
            if (a.getBoolean(R.styleable.LightTextInput_lightAutoFocus, false)) {
                editText.requestFocus();
            }
            a.recycle();
        }
    }

    public void setOnChangeListener(OnChangeListener listener) {
        this.onChangeListener = listener;
    }

    public void setOnSubmitListener(OnSubmitListener listener) {
        this.onSubmitListener = listener;
    }

    public String getText() {
        return editText.getText().toString();
    }

    public void setText(String text) {
        editText.setText(text);
    }

    private int dp(int value) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics()));
    }

}
