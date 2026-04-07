package com.garado.aegis.ui.components.light;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import com.garado.aegis.R;
import com.google.android.material.color.MaterialColors;

/**
 * Full-screen content container matching the Light OS ContentContainer component.
 *
 * Structure:
 *   [LightHeader]  (optional, shown when title is set)
 *   [ScrollView  |  scroll indicator (1dp line, right edge)]
 *
 * The scroll indicator is a thin vertical line on the right edge that shows
 * how far the user has scrolled, matching the custom indicator in the RN version.
 *
 * Attrs: app:lightTitle, app:lightHideBackButton, app:lightContentGap, app:lightContentWidth
 */
public class LightContentContainer extends LinearLayout {

    private final LightHeader header;
    private final ScrollView scrollView;
    private final LinearLayout contentView;
    private final View scrollIndicatorThumb;
    private final View scrollIndicatorTrack;

    public LightContentContainer(Context context) {
        this(context, null);
    }

    public LightContentContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LightContentContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setOrientation(VERTICAL);

        int fgColor = MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnBackground, 0xFFFFFFFF);

        // Header
        header = new LightHeader(context);
        header.setVisibility(GONE);
        addView(header, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        // Scroll area: ScrollView + indicator in a FrameLayout
        FrameLayout scrollWrapper = new FrameLayout(context);
        LayoutParams wrapperParams = new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f);
        addView(scrollWrapper, wrapperParams);

        scrollView = new ScrollView(context);
        scrollView.setOverScrollMode(OVER_SCROLL_NEVER);
        scrollView.setVerticalScrollBarEnabled(false);
        scrollWrapper.addView(scrollView, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        contentView = new LinearLayout(context);
        contentView.setOrientation(VERTICAL);
        scrollView.addView(contentView, new ScrollView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        // Scroll indicator track (1dp wide, full height, right edge)
        scrollIndicatorTrack = new View(context);
        scrollIndicatorTrack.setBackgroundColor(fgColor & 0x22FFFFFF); // very subtle track
        FrameLayout.LayoutParams trackParams = new FrameLayout.LayoutParams(dp(1), LayoutParams.MATCH_PARENT);
        trackParams.rightMargin = dp(6);
        trackParams.gravity = android.view.Gravity.END;
        scrollWrapper.addView(scrollIndicatorTrack, trackParams);

        // Scroll indicator thumb
        scrollIndicatorThumb = new View(context);
        scrollIndicatorThumb.setBackgroundColor(fgColor);
        FrameLayout.LayoutParams thumbParams = new FrameLayout.LayoutParams(dp(3), 0);
        thumbParams.rightMargin = dp(5);
        thumbParams.gravity = android.view.Gravity.END | android.view.Gravity.TOP;
        scrollWrapper.addView(scrollIndicatorThumb, thumbParams);

        setupScrollIndicator();

        // Parse attrs
        int contentGap = dp(24);
        int paddingH = dp(28);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LightContentContainer);
            String title = a.getString(R.styleable.LightContentContainer_lightTitle);
            if (title != null) setTitle(title);
            if (a.getBoolean(R.styleable.LightContentContainer_lightHideBackButton, false)) {
                header.setHideBackButton(true);
            }
            boolean wide = a.getBoolean(R.styleable.LightContentContainer_lightWideContent, false);
            if (wide) paddingH = dp(16);
            contentGap = a.getDimensionPixelSize(R.styleable.LightContentContainer_lightContentGap, contentGap);
            a.recycle();
        }

        contentView.setPadding(paddingH, dp(8), paddingH, dp(16));
        contentView.setDividerDrawable(makeGapDrawable(context, contentGap));
        contentView.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        initialized = true;
    }

    // -------------------------------------------------------------------------

    public void setTitle(String title) {
        header.setTitle(title);
        header.setVisibility(VISIBLE);
    }

    public void setHideBackButton(boolean hide) {
        header.setHideBackButton(hide);
    }

    public LightHeader getHeader() {
        return header;
    }

    /** Add a child view to the scrollable content area. */
    public void addContent(View child) {
        contentView.addView(child, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }

    public void addContent(View child, ViewGroup.LayoutParams params) {
        contentView.addView(child, params);
    }

    // Redirect XML children into contentView so you can define content in XML
    private boolean initialized = false;

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (!initialized) {
            super.addView(child, index, params);
        } else {
            contentView.addView(child, params);
        }
    }

    // -------------------------------------------------------------------------
    // Scroll indicator
    // -------------------------------------------------------------------------

    private void setupScrollIndicator() {
        scrollView.getViewTreeObserver().addOnScrollChangedListener(this::updateScrollIndicator);
        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                updateScrollIndicator();
            }
        });
    }

    private void updateScrollIndicator() {
        int contentHeight = contentView.getHeight();
        int viewportHeight = scrollView.getHeight();
        if (contentHeight <= viewportHeight) {
            scrollIndicatorThumb.setVisibility(GONE);
            scrollIndicatorTrack.setVisibility(GONE);
            return;
        }

        scrollIndicatorThumb.setVisibility(VISIBLE);
        scrollIndicatorTrack.setVisibility(VISIBLE);

        float ratio = (float) viewportHeight / contentHeight;
        int thumbHeight = Math.max(dp(20), (int) (viewportHeight * ratio));

        int scrollY = scrollView.getScrollY();
        int maxScroll = contentHeight - viewportHeight;
        float scrollFraction = maxScroll > 0 ? (float) scrollY / maxScroll : 0f;
        int maxThumbTop = viewportHeight - thumbHeight;
        int thumbTop = (int) (scrollFraction * maxThumbTop);

        FrameLayout.LayoutParams thumbParams = (FrameLayout.LayoutParams) scrollIndicatorThumb.getLayoutParams();
        thumbParams.height = thumbHeight;
        thumbParams.topMargin = thumbTop;
        scrollIndicatorThumb.setLayoutParams(thumbParams);
    }

    // -------------------------------------------------------------------------

    private android.graphics.drawable.ColorDrawable makeGapDrawable(Context context, int height) {
        // A transparent drawable of the given height used as a divider (gap)
        android.graphics.drawable.ColorDrawable d = new android.graphics.drawable.ColorDrawable(0x00000000);
        d.setBounds(0, 0, 0, height);
        return d;
    }

    private int dp(int value) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics()));
    }
}
