package com.garado.aegis.ui.components.light;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

/**
 * A vertical group of selectable views with mutual exclusion (like RadioGroup).
 * Works with LightButton, LightSelectorButton, or any View that uses setSelected().
 *
 * Usage in XML: drop any selectable children in directly:
 *   <com.garado.aegis.ui.components.light.LightOptionGroup ...>
 *       <com.garado.aegis.ui.components.light.LightSelectorButton ... />
 *       <com.garado.aegis.ui.components.light.LightSelectorButton ... />
 *   </com.garado.aegis.ui.components.light.LightOptionGroup>
 */
public class LightOptionGroup extends LinearLayout {

    public interface OnSelectionChangedListener {
        void onSelectionChanged(int index, View selected);
    }

    private OnSelectionChangedListener listener;
    private int selectedIndex = -1;

    public LightOptionGroup(Context context) {
        this(context, null);
    }

    public LightOptionGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LightOptionGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        child.setOnClickListener(v -> select(indexOfChild(v)));
    }

    public void select(int index) {
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).setSelected(i == index);
        }
        selectedIndex = index;
        if (listener != null) listener.onSelectionChanged(index, getChildAt(index));
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public View getSelectedView() {
        if (selectedIndex < 0 || selectedIndex >= getChildCount()) return null;
        return getChildAt(selectedIndex);
    }

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.listener = listener;
    }

    /** RadioGroup-compatible: select by view ID. */
    public void check(int viewId) {
        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i).getId() == viewId) {
                select(i);
                return;
            }
        }
    }

    /** RadioGroup-compatible: returns the ID of the selected child, or -1 if none. */
    public int getCheckedRadioButtonId() {
        if (selectedIndex < 0 || selectedIndex >= getChildCount()) return -1;
        return getChildAt(selectedIndex).getId();
    }
}
