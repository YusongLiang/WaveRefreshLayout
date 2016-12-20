package com.felix.waverefreshlayout.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Felix
 */
public class WaveRefreshLayout extends LinearLayout {

    @IntDef({TYPE_BACKGROUND, TYPE_DARK_WAVE, TYPE_LIGHT_WAVE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LineType {

    }

    private static final int TYPE_BACKGROUND = 0;

    private static final int TYPE_DARK_WAVE = 1;

    private static final int TYPE_LIGHT_WAVE = 2;

    @IntDef({TYPE_CHILD_NONE, TYPE_CHILD_HEADER, TYPE_CHILD_FOOTER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ChildType {

    }

    public static final int TYPE_CHILD_NONE = 0;

    public static final int TYPE_CHILD_HEADER = 1;

    public static final int TYPE_CHILD_FOOTER = 2;

    @IntDef({STATE_WAVE_HIDE, STATE_HEADER_HIDE, STATE_NORMAL, STATE_PULL_TO_REFRESH, STATE_REFRESHABLE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {

    }

    /**
     * 上滑到头部完全隐藏状态
     */
    private static final int STATE_HEADER_HIDE = 0;

    /**
     * 上滑至波浪底部隐藏
     */
    private static final int STATE_WAVE_HIDE = 1;

    /**
     * 轻微上滑及无滑动状态
     */
    private static final int STATE_NORMAL = 2;

    /**
     * 下滑但未达到刷新高度状态
     */
    private static final int STATE_PULL_TO_REFRESH = 3;

    /**
     * 下滑达到可刷新高度状态
     */
    private static final int STATE_REFRESHABLE = 4;

    /**
     * 太阳的光线数
     */
    private static final int NUMBER_OF_SUNSHINE = 12;

    /**
     * 初始的水波基线偏移
     */
    private static final int INITIAL_WAVE_BASELINE_OFFSET = 36;

    /**
     * 刷新需要达到的最小高度
     */
    private static final int MIN_REFRESH_HEIGHT = 300;

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
     * 太阳颜色
     */
    private int mSunColor;

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
     * 太阳画笔
     */
    private Paint mSunPaint;

    /**
     * 浅色波浪路径
     */
    private Path mLightWavePath;

    /**
     * 深色波浪路径
     */
    private Path mDarkWavePath;

    /**
     * 背景路径
     */
    private Path mBackgroundPath;

    /**
     * 阳光路径
     */
    private Path mSunshinePath;

    /**
     * 阳光内部圆的半径
     */
    private int mRadiusOuter;

    /**
     * 太阳中心圆的半径
     */
    private int mRadiusInner;

    /**
     * 阳光光线长度
     */
    private int mSunshineLength;

    private int mHeaderBottom = -1;
    private float mPeakHeight;
    private int mWaveBaselineY = INITIAL_WAVE_BASELINE_OFFSET;
    private int mWaveWidth;
    private PointF mCtrl = new PointF();
    private PointF mDst = new PointF();
    private Thread mHorizontalMoveThread;
    private float mHorizontalOffset;
    private int mLastY;
    private int mTopY;

    @State
    private int mStateIndex;

    public WaveRefreshLayout(Context context) {
        this(context, null);
    }

    public WaveRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initFromAttributes(context, attrs);
        initPaints();
        initSettings();
        initSunshinePath();
    }

    private void initFromAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WaveRefreshLayout);
        mWaveColorLight = a.getColor(R.styleable.WaveRefreshLayout_colorWaveLight, 0xFF2186f3);
        mWaveColorDark = a.getColor(R.styleable.WaveRefreshLayout_colorWaveDark, 0xFF175daa);
        mBackgroundColor = a.getColor(R.styleable.WaveRefreshLayout_colorBackground, 0xFF64A8D1);
        int initialPeakHeight = a.getDimensionPixelSize(R.styleable.WaveRefreshLayout_peakHeight, 16);
        setPeakHeight(initialPeakHeight);
        mWaveWidth = a.getDimensionPixelSize(R.styleable.WaveRefreshLayout_waveWidth, 200);
        mSunColor = a.getColor(R.styleable.WaveRefreshLayout_colorSun, 0xFFFFC900);
        mSunshineLength = a.getDimensionPixelOffset(R.styleable.WaveRefreshLayout_sunshineLength, 16);
        mRadiusOuter = a.getDimensionPixelOffset(R.styleable.WaveRefreshLayout_sunRadius, 36);
        mRadiusInner = mRadiusOuter - 9;
        a.recycle();
    }

    private void initPaints() {
        mWavePaintLight = new Paint(Paint.ANTI_ALIAS_FLAG);
        mWavePaintLight.setColor(mWaveColorLight);
        mWavePaintLight.setStyle(Paint.Style.FILL_AND_STROKE);
        mWavePaintLight.setAlpha(128);
        mWavePaintDark = new Paint(Paint.ANTI_ALIAS_FLAG);
        mWavePaintDark.setColor(mWaveColorDark);
        mWavePaintDark.setStyle(Paint.Style.FILL_AND_STROKE);
        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setColor(mBackgroundColor);
        mBackgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mSunPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSunPaint.setColor(mSunColor);
        mSunPaint.setStyle(Paint.Style.FILL);
    }

    private void initSettings() {
        setClickable(true);
        setOrientation(VERTICAL);
        setWillNotDraw(false);
        mStateIndex = STATE_NORMAL;
    }

    /**
     * 初始化阳光路径
     */
    private void initSunshinePath() {
        mSunshinePath = new Path();
        final int dis = mRadiusOuter + mSunshineLength;
        float degree = 180f / NUMBER_OF_SUNSHINE;
        float radian = (float) Math.toRadians(degree);
        mSunshinePath.moveTo(0, -dis);
        float j = radian;
        for (int i = 0; i < NUMBER_OF_SUNSHINE; i++, j += 2 * radian) {
            mSunshinePath.lineTo((float) (mRadiusOuter * Math.sin(j)), -(float) (mRadiusOuter * Math.cos(j)));
            mSunshinePath.lineTo((float) (dis * Math.sin(j + radian)), -(float) (dis * Math.cos(radian + j)));
        }
        mSunshinePath.close();
        mSunshinePath.addCircle(0, 0, mRadiusOuter - 3, Path.Direction.CCW);
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
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
                            if (mStateIndex > STATE_WAVE_HIDE) {
                                mHorizontalOffset--;
                                mHorizontalOffset %= (mWaveWidth * 2);
                                postInvalidate();
                                Thread.sleep(16);
                            }
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
        View headerView = findChildByType(TYPE_CHILD_HEADER);
        if (headerView == null) {
            final View child = getChildAt(0);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            return lp.bottomMargin + child.getBottom();
        } else {
            LayoutParams lp = (LayoutParams) headerView.getLayoutParams();
            return lp.bottomMargin + headerView.getBottom();
        }
    }

    private View findChildByType(@ChildType int target) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (lp.childType == target) {
                return child;
            }
        }
        return null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mHeaderBottom == -1) mHeaderBottom = getHeaderBottom();
        if (mStateIndex > STATE_WAVE_HIDE) drawBackground(canvas);
        drawSun(canvas);
        if (mStateIndex > STATE_HEADER_HIDE) drawDarkWave(canvas);
        if (mStateIndex > STATE_WAVE_HIDE) drawLightWave(canvas);
    }

    private void addWaveLineToPath(Path src, @LineType int type) {
        final int width = getWidth() + 2 * mWaveWidth;
        for (int i = 0, j = 0; i < width; i += mWaveWidth, j++) {
            mCtrl.x = mWaveWidth * 0.5f;
            switch (type) {
                case TYPE_BACKGROUND:
                    mCtrl.y = mPeakHeight;
                    break;
                case TYPE_DARK_WAVE:
                    mCtrl.y = j % 2 == 0 ? -mPeakHeight : mPeakHeight;
                    break;
                case TYPE_LIGHT_WAVE:
                    mCtrl.y = j % 2 == 0 ? mPeakHeight : -mPeakHeight;
                    break;
            }
            mDst.x = mWaveWidth;
            mDst.y = 0;
            src.rQuadTo(mCtrl.x, mCtrl.y, mDst.x, mDst.y);
        }
    }

    private void drawBackground(Canvas canvas) {
        canvas.save();
        canvas.translate(mHorizontalOffset, 0);
        updateBackgroundPath();
        canvas.drawPath(mBackgroundPath, mBackgroundPaint);
        canvas.restore();
    }

    private void drawLightWave(Canvas canvas) {
        canvas.save();
        canvas.translate(mHorizontalOffset, 0);
        updateLightWavePath();
        canvas.drawPath(mLightWavePath, mWavePaintLight);
        canvas.restore();
    }

    private void drawDarkWave(Canvas canvas) {
        canvas.save();
        updateDarkWavePath();
        canvas.translate(mHorizontalOffset, 0);
        canvas.drawPath(mDarkWavePath, mWavePaintDark);
        canvas.restore();
    }

    private void drawSun(Canvas canvas) {
        canvas.save();
        canvas.translate(getWidth() / 2, mTopY + 108);
        canvas.drawPath(mSunshinePath, mSunPaint);
        canvas.drawCircle(0, 0, mRadiusInner, mSunPaint);//绘制太阳中心
        canvas.restore();
    }

    private void updateBackgroundPath() {
        if (mBackgroundPath == null)
            mBackgroundPath = new Path();
        else mBackgroundPath.reset();
        mBackgroundPath.moveTo(0, mTopY);
        mBackgroundPath.rLineTo(0, mWaveBaselineY - mTopY);
        addWaveLineToPath(mBackgroundPath, TYPE_BACKGROUND);
        mBackgroundPath.rLineTo(0, mTopY - mWaveBaselineY);
        mBackgroundPath.lineTo(0, mTopY);
        mBackgroundPath.close();
    }

    private void updateDarkWavePath() {
        if (mDarkWavePath == null) mDarkWavePath = new Path();
        else mDarkWavePath.reset();
        mDarkWavePath.moveTo(0, mHeaderBottom);
        mDarkWavePath.lineTo(0, mWaveBaselineY);
        addWaveLineToPath(mDarkWavePath, TYPE_DARK_WAVE);
        mDarkWavePath.rLineTo(0, mHeaderBottom - mWaveBaselineY);
        mDarkWavePath.lineTo(0, mHeaderBottom);
        mDarkWavePath.close();
    }

    private Path updateLightWavePath() {
        if (mLightWavePath == null) mLightWavePath = new Path();
        else mLightWavePath.reset();
        mLightWavePath.moveTo(0, mWaveBaselineY);
        addWaveLineToPath(mLightWavePath, TYPE_LIGHT_WAVE);
        mLightWavePath.moveTo(0, mWaveBaselineY);
        addWaveLineToPath(mLightWavePath, TYPE_DARK_WAVE);
        mLightWavePath.close();
        return mLightWavePath;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = (int) event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                final int currentY = (int) event.getY();
                int dY = currentY - mLastY;
                if (Math.abs(dY) > 160) {//防止多点触控导致的跳跃
                    mLastY = currentY;
                    return super.onTouchEvent(event);
                }
                if (mTopY < 0 && dY > 0)
                    dY /= (-mTopY / 120 + 1);
                scrollBy(0, -dY);
                mTopY -= dY;
                mPeakHeight += (dY / 10f);
                mLastY = currentY;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                break;
        }
        updateState();
        return super.onTouchEvent(event);
    }

    /**
     * 更新当前状态
     *
     * @see #mStateIndex
     */
    private void updateState() {
        if (mTopY < -MIN_REFRESH_HEIGHT) {
            mStateIndex = STATE_REFRESHABLE;
        } else if (mTopY < 0) {
            mStateIndex = STATE_PULL_TO_REFRESH;
        } else if (mTopY < mWaveBaselineY + mPeakHeight) {
            mStateIndex = STATE_NORMAL;
        } else if (mTopY < mHeaderBottom) {
            mStateIndex = STATE_WAVE_HIDE;
        } else {
            mStateIndex = STATE_HEADER_HIDE;
        }
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

        public LayoutParams(int width, int height, @ChildType int childType) {
            super(width, height);
            this.childType = childType;
        }

        public int getChildType() {
            return childType;
        }

        public void setChildType(@ChildType int childType) {
            this.childType = childType;
        }
    }
}