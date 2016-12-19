package com.felix.waverefreshlayout.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * @author Felix
 */
public class WaveRefreshLayout extends LinearLayout {

    private static final int INITIAL_BASELINE_OFFSET = 36;
    /**
     * 深色波浪颜色
     */
    private int mWaveColorDark;
    /**
     * 浅色波浪颜色
     */
    private int mWaveColorLight;
    /**
     * 背景颜色
     */
    private int mBackgroundColor;
    /**
     * 深色波浪画笔
     */
    private Paint mWavePaintDark;
    /**
     * 浅色波浪画笔
     */
    private Paint mWavePaintLight;
    /**
     * 背景画笔
     */
    private Paint mBackgroundPaint;
    /**
     * 波浪路径
     */
    private Path mWavePath;
    private Path mBackgroundPath;
    private int mWaveBottom = -1;
    private float mPeakHeight;
    private int mWaveBaselineY = INITIAL_BASELINE_OFFSET;
    private int mWaveWidth;
    private PointF mCtrl = new PointF();
    private PointF mDst = new PointF();
    private Thread mHorizontalMoveThread;
    private float mHorizontalOffset;
    private int mLastY;
    private int mTopY;

    public WaveRefreshLayout(Context context) {
        this(context, null);
    }

    public WaveRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initFromAttributes(context, attrs);
        initPaints();
        initSettings();
    }

    private void initFromAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WaveRefreshLayout);
        mWaveColorLight = a.getColor(R.styleable.WaveRefreshLayout_colorWaveLight, 0xFF4380D3);
        mWaveColorDark = a.getColor(R.styleable.WaveRefreshLayout_colorWaveDark, 0xFF0F4FA8);
        mBackgroundColor = a.getColor(R.styleable.WaveRefreshLayout_colorBackground, 0xFF64A8D1);
        int initialPeakHeight = a.getDimensionPixelSize(R.styleable.WaveRefreshLayout_peakHeight, 16);
        setPeakHeight(initialPeakHeight);
        mWaveWidth = a.getDimensionPixelSize(R.styleable.WaveRefreshLayout_waveWidth, 200);
        a.recycle();
    }

    private void initPaints() {
        mWavePaintLight = new Paint(Paint.ANTI_ALIAS_FLAG);
        mWavePaintLight.setColor(mWaveColorLight);
        mWavePaintLight.setStyle(Paint.Style.FILL_AND_STROKE);
        mWavePaintDark = new Paint(Paint.ANTI_ALIAS_FLAG);
        mWavePaintDark.setColor(mWaveColorDark);
        mWavePaintDark.setStyle(Paint.Style.FILL_AND_STROKE);
        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setColor(mBackgroundColor);
        mBackgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    private void initSettings() {
        setClickable(true);
        setOrientation(VERTICAL);
        setWillNotDraw(false);
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        startHorizontalMoveThread();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopHorizontalMoveThread();
    }

    private void stopHorizontalMoveThread() {
        if (mHorizontalMoveThread != null) {
            mHorizontalMoveThread.interrupt();
            mHorizontalMoveThread = null;
        }
    }

    private void startHorizontalMoveThread() {
        if (mHorizontalMoveThread == null) {
            mHorizontalMoveThread = new Thread() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            mHorizontalOffset--;
                            mHorizontalOffset %= (mWaveWidth * 2);
                            postInvalidate();
                            Thread.sleep(16);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            };
            mHorizontalMoveThread.start();
        }
    }

    private int getHeaderBottom() {
        final int count = getChildCount();
        if (count <= 0) return 0;
        View headerView = findChildByType(ChildType.TYPE_CHILD_HEADER);
        if (headerView == null) {
            final View child = getChildAt(0);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            return lp.bottomMargin + child.getBottom();
        } else {
            LayoutParams lp = (LayoutParams) headerView.getLayoutParams();
            return lp.bottomMargin + headerView.getBottom();
        }
    }

    private View findChildByType(int target) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            int type = lp.childType;
            if (type == target) {
                return child;
            }
        }
        return null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mWaveBottom == -1) mWaveBottom = getHeaderBottom();
        canvas.save();
        canvas.translate(mHorizontalOffset, 0);
        drawBackground(canvas);
        drawLightWave(canvas);
        drawDarkWave(canvas);
        canvas.restore();
    }

    private Path addWaveLineToPath(Path src, int type) {
        final int width = getWidth() + 2 * mWaveWidth;
        for (int i = 0, j = 0; i < width; i += mWaveWidth, j++) {
            mCtrl.x = mWaveWidth * 0.5f;
            switch (type) {
                case LineType.TYPE_BACKGROUND:
                    mCtrl.y = -mPeakHeight;
                    break;
                case LineType.TYPE_DARK_WAVE:
                    mCtrl.y = j % 2 == 0 ? -mPeakHeight : mPeakHeight;
                    break;
                case LineType.TYPE_LIGHT_WAVE:
                    mCtrl.y = j % 2 == 0 ? mPeakHeight : -mPeakHeight;
                    break;
            }
            mDst.x = mWaveWidth;
            mDst.y = 0;
            src.rQuadTo(mCtrl.x, mCtrl.y, mDst.x, mDst.y);
        }
        return src;
    }

    private void drawBackground(Canvas canvas) {
        canvas.drawPath(getBackgroundPath(), mBackgroundPaint);
    }

    private void drawLightWave(Canvas canvas) {
        canvas.drawPath(getLightWavePath(), mWavePaintLight);
    }

    private void drawDarkWave(Canvas canvas) {
        canvas.drawPath(getDarkWavePath(), mWavePaintDark);
    }

    private Path getBackgroundPath() {
        if (mBackgroundPath == null)
            mBackgroundPath = new Path();
        else mBackgroundPath.reset();
        mBackgroundPath.moveTo(0, mTopY);
        mBackgroundPath.rLineTo(0, mWaveBaselineY - mTopY);
        addWaveLineToPath(mBackgroundPath, LineType.TYPE_BACKGROUND);
        mBackgroundPath.rLineTo(0, mTopY - mWaveBaselineY);
        mBackgroundPath.lineTo(0, mTopY);
        mBackgroundPath.close();
        return mBackgroundPath;
    }

    private Path getDarkWavePath() {
        if (mWavePath == null) mWavePath = new Path();
        else mWavePath.reset();
        mWavePath.moveTo(0, mWaveBottom);
        mWavePath.lineTo(0, mWaveBaselineY);
        addWaveLineToPath(mWavePath, LineType.TYPE_DARK_WAVE);
        mWavePath.rLineTo(0, mWaveBottom - mWaveBaselineY);
        mWavePath.lineTo(0, mWaveBottom);
        mWavePath.close();
        return mWavePath;
    }

    private Path getLightWavePath() {
        if (mWavePath == null) mWavePath = new Path();
        else mWavePath.reset();
        mWavePath.moveTo(0, mWaveBaselineY);
        addWaveLineToPath(mWavePath, LineType.TYPE_LIGHT_WAVE);
        mWavePath.moveTo(0, mWaveBaselineY);
        addWaveLineToPath(mWavePath, LineType.TYPE_DARK_WAVE);
        mWavePath.close();
        return mWavePath;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = (int) event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                final int currentY = (int) event.getY();
                final int dY = currentY - mLastY;
                scrollBy(0, -dY);
                mTopY -= dY;
                mPeakHeight += (dY / 8f);
                mLastY = currentY;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void setOrientation(int orientation) {
        if (orientation != VERTICAL) throw new IllegalArgumentException("仅支持竖直方向布局");
        super.setOrientation(orientation);
    }

    public float getPeakHeight() {
        return mPeakHeight;
    }

    public void setPeakHeight(int peakHeight) {
        mPeakHeight = peakHeight;
    }

    /**
     * 获取深色波浪的颜色
     *
     * @return 颜色的ARGB值
     */
    public int getWaveColorDark() {
        return mWaveColorDark;
    }

    /**
     * 设置深色波浪的颜色
     *
     * @param color 要设置的颜色
     */
    public void setWaveColorDark(@ColorInt int color) {
        mWaveColorDark = color;
    }

    /**
     * 获取浅色波浪颜色
     *
     * @return 颜色的ARGB值
     */
    public int getWaveColorLight() {
        return mWaveColorLight;
    }

    /**
     * 设置浅色波浪的颜色
     *
     * @param color 要设置的颜色
     */
    public void setWaveColorLight(@ColorInt int color) {
        mWaveColorLight = color;
    }

    @Override
    protected LinearLayout.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        return new LayoutParams(lp);
    }

    @Override
    public LinearLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    public interface ChildType {

        int TYPE_CHILD_NONE = 0;

        int TYPE_CHILD_HEADER = 1;

        int TYPE_CHILD_FOOTER = 2;
    }

    public interface LineType {

        int TYPE_BACKGROUND = 0;

        int TYPE_DARK_WAVE = 1;

        int TYPE_LIGHT_WAVE = 2;
    }

    public static class LayoutParams extends LinearLayout.LayoutParams {

        public int childType;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.WaveRefreshLayout_Layout);
            childType = a.getInteger(R.styleable.WaveRefreshLayout_Layout_childType, 0);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, float weight) {
            super(width, height, weight);
        }

        public LayoutParams(ViewGroup.LayoutParams p) {
            super(p);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public LayoutParams(LinearLayout.LayoutParams source) {
            super(source);
        }
    }
}
