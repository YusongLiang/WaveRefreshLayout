package com.felix.waverefreshlayout.library;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.Scroller;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Felix
 */
public class WaveRefreshLayout extends LinearLayout {

    /**
     * 加载刷新数据完成
     */
    private static final int MESSAGE_LOAD_DATA_FINISH = 1;

    /**
     * 最小波浪高度
     */
    private static final int MIN_WAVE_HEIGHT = 56;

    /**
     * 线条类型注解，所修饰的变量仅可取{@link #TYPE_BACKGROUND},{@link #TYPE_DARK_WAVE}和{@link #TYPE_LIGHT_WAVE}中的一种
     */
    @IntDef({TYPE_BACKGROUND, TYPE_DARK_WAVE, TYPE_LIGHT_WAVE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LineType {

    }

    /**
     * 波浪背景边界线类型曲线
     */
    private static final int TYPE_BACKGROUND = 0;

    /**
     * 深色波浪边界类型曲线
     */
    private static final int TYPE_DARK_WAVE = 1;

    /**
     * 浅色波浪边界类型曲线
     */
    private static final int TYPE_LIGHT_WAVE = 2;

    /**
     * 子控件类型注解，修饰变量可取{@link #TYPE_CHILD_NONE},{@link #TYPE_CHILD_HEADER}和{@link #TYPE_CHILD_FOOTER}之一
     */
    @IntDef({TYPE_CHILD_NONE, TYPE_CHILD_HEADER, TYPE_CHILD_FOOTER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ChildType {

    }

    /**
     * 子控件类型无，为子控件默认类型
     */
    public static final int TYPE_CHILD_NONE = 0;

    /**
     * 子控件类型头部，仅允许有一个
     */
    public static final int TYPE_CHILD_HEADER = 1;

    /**
     * 子控件类型脚部，仅允许有一个
     */
    public static final int TYPE_CHILD_FOOTER = 2;

    /**
     * 状态类型注解，所修饰变量可取
     * {@link #STATE_WAVE_HIDE}，
     * {@link #STATE_HEADER_HIDE}，
     * {@link #STATE_NORMAL}，
     * {@link #STATE_PULL_TO_REFRESH}，
     * {@link #STATE_SHOW_SUN}，
     * {@link #STATE_REFRESHABLE}之一
     * 界面上滑时对应数值逐渐减小，下滑时则增大
     */
    @IntDef({STATE_WAVE_HIDE, STATE_HEADER_HIDE, STATE_NORMAL, STATE_PULL_TO_REFRESH, STATE_SHOW_SUN, STATE_REFRESHABLE})
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
     * 太阳可见
     */
    private static final int STATE_SHOW_SUN = 4;

    /**
     * 下滑达到可刷新高度状态
     */
    private static final int STATE_REFRESHABLE = 5;

    /**
     * 太阳的光线数
     */
    private static final int NUMBER_OF_SUNSHINE = 12;

    /**
     * 初始的水波基线偏移
     */
    private static final int WAVE_BASELINE_OFFSET = 36;

    /**
     * 太阳中心偏移
     */
    private static final int SUN_CENTER_OFFSET = 108;

    /**
     * 刷新需要达到的最小高度
     */
    private static final int MIN_REFRESH_HEIGHT = 200;

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
     * 云画笔
     */
    private Paint mCloudPaint;

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
     * 太阳路径
     */
    private Path mSunPath;

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

    /**
     * 头部底坐标
     */
    private int mHeaderBottom = -1;

    /**
     * 波峰初始高度
     */
    private int mInitialPeakHeight;

    /**
     * 波峰高度
     */
    private float mPeakHeight;

    /**
     * 水波宽度
     */
    private int mWaveWidth;

    /**
     * 贝塞尔曲线控制点
     *
     * @see #addWaveLineToPath(Path, int)
     */
    private PointF mCtrl = new PointF();

    /**
     * 贝塞尔曲线目标点
     *
     * @see #addWaveLineToPath(Path, int)
     */
    private PointF mDst = new PointF();

    /**
     * 水平移动线程
     *
     * @see #startUpdateViewThread()
     * @see #stopUpdateViewThread()
     */
    private Thread mUpdateThread;

    /**
     * 波浪水平偏移
     */
    private float mHorizontalOffset;

    /**
     * 上一个触摸点的纵坐标
     */
    private float mLastY;

    /**
     * 太阳旋转角度
     */
    private float mSunRotateDegree;

    /**
     * 页面当前状态
     */
    @State
    private int mStateIndex;

    /**
     * 弹回动画
     */
    private ValueAnimator mRestoreAnim;

    /**
     * 开始弹回时的ScrollY值
     */
    private int mScrollYRestoreFrom;

    /**
     * 结束后是否会进入刷新状态
     */
    private boolean mRefreshWhenFinish;

    /**
     * 是否允许刷新功能
     */
    private boolean mIsRefreshable;

    /**
     * 是否正在刷新
     */
    private boolean mIsRefreshing;

    /**
     * 刷新监听器
     */
    private OnRefreshListener mOnRefreshListener;

    /**
     * 云朵位图对象
     */
    private Bitmap mCloudBitmap;

    /**
     * 云宽度
     */
    private int mCloudWidth;

    /**
     * 云高度
     */
    private int mCloudHeight;

    /**
     * 云位置横坐标
     */
    private int mCloudX;

    private VelocityTracker mVelocityTracker;

    private Scroller mScroller;

    private int mScrollBottom = 20000;

    private long mRestoreDuration;

    public WaveRefreshLayout(Context context) {
        this(context, null);
    }

    public WaveRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initFromAttributes(context, attrs);
        initPaints();
        initLayoutSettings();
        initSunshinePath();
        initCloudBitmap();
        initAnimator();
    }

    private void initFromAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WaveRefreshLayout);
        mWaveColorLight = a.getColor(R.styleable.WaveRefreshLayout_colorWaveLight, 0xFF2186F3);
        mWaveColorDark = a.getColor(R.styleable.WaveRefreshLayout_colorWaveDark, 0xFF175DAA);
        mBackgroundColor = a.getColor(R.styleable.WaveRefreshLayout_colorBackground, 0xFF64A8D1);
        mInitialPeakHeight = a.getDimensionPixelSize(R.styleable.WaveRefreshLayout_peakHeight, 16);
        mWaveWidth = a.getDimensionPixelSize(R.styleable.WaveRefreshLayout_waveWidth, 200);
        mSunColor = a.getColor(R.styleable.WaveRefreshLayout_colorSun, 0xFFFFC900);
        mSunshineLength = a.getDimensionPixelOffset(R.styleable.WaveRefreshLayout_sunshineLength, 16);
        mIsRefreshable = a.getBoolean(R.styleable.WaveRefreshLayout_refreshable, true);
        mRestoreDuration = a.getInt(R.styleable.WaveRefreshLayout_restoreDuration, 200);
        mCloudWidth = a.getDimensionPixelSize(R.styleable.WaveRefreshLayout_cloudWidth, 108);
        mCloudHeight = a.getDimensionPixelOffset(R.styleable.WaveRefreshLayout_cloudHeight, 72);
        mRadiusOuter = a.getDimensionPixelOffset(R.styleable.WaveRefreshLayout_sunRadius, 36);
        mRadiusInner = mRadiusOuter - 9;
        a.recycle();
    }

    /**
     * 初始化画笔
     */
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
        mCloudPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    /**
     * 初始化布局设置
     */
    private void initLayoutSettings() {
        setClickable(true);
        setFocusable(true);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        setOrientation(VERTICAL);
        setWillNotDraw(false);
        mStateIndex = STATE_NORMAL;
        mScroller = new Scroller(getContext());
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

    /**
     * 初始化云朵位图
     */
    private void initCloudBitmap() {
        Bitmap temp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_cloud);
        mCloudBitmap = Bitmap.createScaledBitmap(temp, mCloudWidth, mCloudHeight, true);
    }

    /**
     * 初始化动画
     */
    private void initAnimator() {
        mRestoreAnim = ValueAnimator.ofFloat(1, 0);
        mRestoreAnim.setDuration(mRestoreDuration);
        mRestoreAnim.setInterpolator(new DecelerateInterpolator());
        mRestoreAnim.addListener(mAnimListener);
        mRestoreAnim.addUpdateListener(mUpdateListener);
    }

    private ValueAnimator.AnimatorUpdateListener mUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float animatedValue = (float) animation.getAnimatedValue();
            restoreView(animatedValue);
        }
    };

    /**
     * 恢复控件，弹回初始位置或弹回到刷新位置
     *
     * @param percentage 回弹百分比
     */
    private void restoreView(float percentage) {
        int scrollY;
        if (mRefreshWhenFinish) //执行刷新，弹回到刷新高度
            scrollY = (int) ((mScrollYRestoreFrom + MIN_REFRESH_HEIGHT) * percentage) - MIN_REFRESH_HEIGHT;
        else //不执行刷新，直接弹回初始位置
            scrollY = (int) (mScrollYRestoreFrom * percentage);
        scrollTo(0, scrollY);
        updateState();
        updateDrawParams();
    }

    private Animator.AnimatorListener mAnimListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationEnd(Animator animation) {
            if (mOnRefreshListener != null && mIsRefreshing) {
                if (mRefreshWhenFinish) {
                    mIsRefreshing = true;
                    mOnRefreshListener.onAcquireData();
                } else {
                    mOnRefreshListener.onLoadData();
                    mRefreshWhenFinish = false;
                    mIsRefreshing = false;
                }
            }
        }

        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    };

    /**
     * 完成刷新
     * 在{@link OnRefreshListener#onAcquireData()} 内设置刷新时执行的操作，刷新结束时调用该方法
     * 需要{@link #setIsRefreshable(boolean)}置为true
     */
    public void finishRefresh() {
        finishRefresh(false);
    }

    /**
     * 完成刷新
     * 在{@link OnRefreshListener#onAcquireData()} 内设置刷新时执行的操作，刷新结束时调用该方法
     * 需要{@link #setIsRefreshable(boolean)}置为true
     *
     * @param isCancel 是否放弃刷新
     */
    private void finishRefresh(boolean isCancel) {
        if (mIsRefreshable && mIsRefreshing) {
            mCloudX = 0;
            mRefreshWhenFinish = false;
            mScrollYRestoreFrom = -MIN_REFRESH_HEIGHT;
            if (mStateIndex == STATE_REFRESHABLE && !isCancel) {
                mRestoreAnim.start();
            }
        }
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
        startUpdateViewThread();
        updateDrawParams();
        Rect rect = new Rect();
        getGlobalVisibleRect(rect);
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        canvas.translate(0, getHeaderBottom());
        return super.drawChild(canvas, child, drawingTime);
    }

    /**
     * 结束更新控件线程
     */
    private void stopUpdateViewThread() {
        if (mUpdateThread != null) {
            mUpdateThread.interrupt();
            mUpdateThread = null;
        }
    }

    /**
     * 开始更新控件线程
     */
    private void startUpdateViewThread() {
        if (mUpdateThread == null) {
            mUpdateThread = new Thread() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            if (mStateIndex > STATE_WAVE_HIDE) {
                                mHorizontalOffset--;
                                mHorizontalOffset %= (mWaveWidth * 2);
                                if (mIsRefreshable && mIsRefreshing) {
                                    mSunRotateDegree += 5;
                                    mCloudX += 2;
                                    final int distance = getWidth() + mCloudWidth;
                                    mCloudX %= distance;
                                }
                                postInvalidate();
                                Thread.sleep(16);
                            }
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            };
            mUpdateThread.start();
        }
    }

    /**
     * 获取头部底坐标
     *
     * @return 底部纵坐标
     */
    private int getHeaderBottom() {
        final int count = getChildCount();
        if (count <= 0) return 0;
        View headerView = findChildByType(TYPE_CHILD_HEADER);
        if (headerView == null) {
            final View child = getChildAt(0);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            return WAVE_BASELINE_OFFSET + mInitialPeakHeight + MIN_WAVE_HEIGHT + lp.topMargin + child.getTop();
        } else {
            LayoutParams lp = (LayoutParams) headerView.getLayoutParams();
            return lp.bottomMargin + headerView.getBottom();
        }
    }

    /**
     * 通过类型找到子控件
     *
     * @param target 类型值
     * @return 找到的第一个该类型子控件
     */
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
        final int scrollY = getScrollY();
        if (mHeaderBottom == -1) mHeaderBottom = getHeaderBottom();
        if (mStateIndex > STATE_WAVE_HIDE) drawBackground(canvas);
        if (mIsRefreshing && mIsRefreshable)
            drawCloud(mCloudX, scrollY + SUN_CENTER_OFFSET - 56, 200, false, canvas);
        if (mStateIndex >= STATE_SHOW_SUN) drawSun(canvas);
        if (mIsRefreshing && mIsRefreshable)
            drawCloud(mCloudX, scrollY + SUN_CENTER_OFFSET - 16, 255, true, canvas);
        if (mStateIndex > STATE_HEADER_HIDE) drawDarkWave(canvas);
        if (mStateIndex > STATE_WAVE_HIDE) drawLightWave(canvas);
    }

    /**
     * 绘制背景
     *
     * @param canvas 画布
     */
    private void drawBackground(Canvas canvas) {
        canvas.save();
        canvas.translate(mHorizontalOffset, 0);
        updateBackgroundPath();
        canvas.drawPath(mBackgroundPath, mBackgroundPaint);
        canvas.restore();
    }

    /**
     * 绘制浅色波浪
     *
     * @param canvas 画布
     */
    private void drawLightWave(Canvas canvas) {
        canvas.save();
        canvas.translate(mHorizontalOffset, 0);
        updateLightWavePath();
        canvas.drawPath(mLightWavePath, mWavePaintLight);
        canvas.restore();
    }

    /**
     * 绘制深色波浪
     *
     * @param canvas 画布
     */
    private void drawDarkWave(Canvas canvas) {
        canvas.save();
        updateDarkWavePath();
        canvas.translate(mHorizontalOffset, 0);
        canvas.drawPath(mDarkWavePath, mWavePaintDark);
        canvas.restore();
    }

    /**
     * 绘制太阳
     *
     * @param canvas 画布
     */
    private void drawSun(Canvas canvas) {
        final int scrollY = getScrollY();
        canvas.save();
        canvas.translate(getWidth() / 2, scrollY + SUN_CENTER_OFFSET);
        canvas.rotate(mSunRotateDegree, 0, 0);
        updateSunPath();
        canvas.drawPath(mSunPath, mSunPaint);
        canvas.restore();
    }

    /**
     * 绘制云朵
     *
     * @param x      云朵横坐标
     * @param y      云朵纵坐标
     * @param alpha  云朵透明度
     * @param isRtl  是否为从右至左
     * @param canvas 画布
     */
    private void drawCloud(int x, int y, int alpha, boolean isRtl, Canvas canvas) {
        mCloudPaint.setAlpha(alpha);
        canvas.save();
        if (isRtl) canvas.scale(-1, 1, getWidth() / 2, 0);
        canvas.drawBitmap(mCloudBitmap, x, y, mCloudPaint);
        canvas.restore();
    }

    /**
     * 更新背景路径
     */
    private void updateBackgroundPath() {
        if (mBackgroundPath == null)
            mBackgroundPath = new Path();
        else mBackgroundPath.reset();
        final int scrollY = getScrollY();
        mBackgroundPath.moveTo(0, scrollY);
        mBackgroundPath.rLineTo(0, WAVE_BASELINE_OFFSET - scrollY);
        addWaveLineToPath(mBackgroundPath, TYPE_BACKGROUND);
        mBackgroundPath.rLineTo(0, scrollY - WAVE_BASELINE_OFFSET);
        mBackgroundPath.lineTo(0, scrollY);
        mBackgroundPath.close();
    }

    /**
     * 更新深色波浪路径
     */
    private void updateDarkWavePath() {
        if (mDarkWavePath == null) mDarkWavePath = new Path();
        else mDarkWavePath.reset();
        mDarkWavePath.moveTo(0, mHeaderBottom);
        mDarkWavePath.lineTo(0, WAVE_BASELINE_OFFSET);
        addWaveLineToPath(mDarkWavePath, TYPE_DARK_WAVE);
        mDarkWavePath.rLineTo(0, mHeaderBottom - WAVE_BASELINE_OFFSET);
        mDarkWavePath.lineTo(0, mHeaderBottom);
        mDarkWavePath.close();
    }

    /**
     * 更新浅色波浪路径
     */
    private void updateLightWavePath() {
        if (mLightWavePath == null) mLightWavePath = new Path();
        else mLightWavePath.reset();
        mLightWavePath.moveTo(0, WAVE_BASELINE_OFFSET);
        addWaveLineToPath(mLightWavePath, TYPE_LIGHT_WAVE);
        mLightWavePath.moveTo(0, WAVE_BASELINE_OFFSET);
        addWaveLineToPath(mLightWavePath, TYPE_DARK_WAVE);
        mLightWavePath.close();
    }

    /**
     * 在制定路径上添加波浪线
     *
     * @param src  需要添加波浪线的路径
     * @param type 波浪线类型
     */
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

    /**
     * 更新太阳路径
     */
    private void updateSunPath() {
        if (mSunPath == null) mSunPath = new Path();
        else mSunPath.reset();
        mSunPath.addPath(mSunshinePath);
        mSunPath.addCircle(0, 0, mRadiusInner, Path.Direction.CW);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return onTouch(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return onTouch(ev);
    }

    private boolean onTouch(MotionEvent event) {
        initVelocityTracker(event);
        final int scrollY = getScrollY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) mScroller.abortAnimation();
                mLastY = (int) event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float currentY = event.getY();
                int dY = (int) (currentY - mLastY);
                mLastY = currentY;
                if (Math.abs(dY) > 160) {//防止多点触控导致的跳跃
                    return true;
                }
                if (scrollY < 0 && dY > 0) {
                    dY /= (-scrollY / 160f + 1);
                }
                scrollBy(0, -dY);
                updateDrawParams();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mStateIndex > STATE_NORMAL) {
                    mScrollYRestoreFrom = scrollY;
                    mIsRefreshing = mStateIndex == STATE_REFRESHABLE && mIsRefreshable;
                    mRefreshWhenFinish = mIsRefreshing;
                    mRestoreAnim.start();
                } else {
                    mVelocityTracker.computeCurrentVelocity(1000);
                    int velocityY = (int) mVelocityTracker.getYVelocity();
                    fling(-velocityY);
                }
                break;
        }
        updateState();
        return true;
    }

    /**
     * 初始化速度追踪器
     *
     * @param event {@link MotionEvent}对象
     *              从{@link #onTouchEvent(MotionEvent)}或{@link #onInterceptTouchEvent(MotionEvent)}中获取
     * @see #onTouch(MotionEvent)
     * @see #onTouchEvent(MotionEvent)
     * @see #onInterceptTouchEvent(MotionEvent)
     * @see #releaseVelocityTracker()
     */
    private void initVelocityTracker(MotionEvent event) {
        if (mVelocityTracker == null)
            mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(event);
    }

    /**
     * 释放速度追踪器
     *
     * @see #initVelocityTracker(MotionEvent)
     */
    private void releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    /**
     * 执行Y方向上的猛冲操作
     *
     * @param velocityY Y方向速度
     */
    private void fling(int velocityY) {
        final int scrollX = getScrollX();
        final int scrollY = getScrollY();
        mScroller.fling(scrollX, scrollY, 0, velocityY, 0, 0, 0, mScrollBottom);
        invalidate();
    }

    @Override
    public void computeScroll() {
        updateDrawParams();
        updateState();
        if (mScroller.computeScrollOffset()) {
            final int scrollX = mScroller.getCurrX();
            final int scrollY = mScroller.getCurrY();
            scrollTo(scrollX, scrollY);
            postInvalidate();
        }
    }

    /**
     * 更新绘制参数，在scrollY 发生变化后调用
     *
     * @see #getScrollY()
     */
    private void updateDrawParams() {
        final int scrollY = getScrollY();
        mPeakHeight = mInitialPeakHeight - scrollY / 16f;
        if (!mIsRefreshing) rotateSunTo(scrollY / 3f);
    }

    /**
     * 更新当前状态
     *
     * @see #mStateIndex
     */
    private void updateState() {
        final int scrollY = getScrollY();
        final int showSunY = (int) (WAVE_BASELINE_OFFSET + mPeakHeight + mRadiusOuter + mSunshineLength - SUN_CENTER_OFFSET);
        if (scrollY <= -MIN_REFRESH_HEIGHT) {
            mStateIndex = STATE_REFRESHABLE;
        } else if (scrollY < showSunY) {
            mStateIndex = STATE_SHOW_SUN;
        } else if (scrollY < 0) {
            mStateIndex = STATE_PULL_TO_REFRESH;
        } else if (scrollY < WAVE_BASELINE_OFFSET + mPeakHeight) {
            mStateIndex = STATE_NORMAL;
        } else if (scrollY < mHeaderBottom) {
            mStateIndex = STATE_WAVE_HIDE;
        } else {
            mStateIndex = STATE_HEADER_HIDE;
        }
        if (mIsRefreshing) {
            if (mStateIndex != STATE_REFRESHABLE) {//刷新过程中被滑回
                finishRefresh(true);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopUpdateViewThread();
        releaseVelocityTracker();
    }

    // 此处以下为setter和getter

    @Override
    public void setOrientation(int orientation) {
        if (orientation != VERTICAL) throw new IllegalArgumentException("仅支持竖直方向布局");
        super.setOrientation(orientation);
    }

    /**
     * 将太阳旋转指定角度
     *
     * @param degree 旋转的角度值
     */
    private void rotateSunBy(float degree) {
        mSunRotateDegree += degree;
    }

    /**
     * 将太阳旋转到制定角度
     *
     * @param degree 目标角度
     */
    private void rotateSunTo(float degree) {
        mSunRotateDegree = degree;
    }

    /**
     * 获取波峰初始高度
     *
     * @return 波峰初始高度
     */
    public float getInitialPeakHeight() {
        return mInitialPeakHeight;
    }

    /**
     * 设置波峰初始高度
     *
     * @param height 波峰初始高度
     */
    public void setInitialPeakHeight(int height) {
        mInitialPeakHeight = height;
    }

    /**
     * 获取深色波浪的颜色
     *
     * @return 颜色值
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
     * @return 颜色值
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

    /**
     * 获取拖回弹回时间
     *
     * @return 回弹时间的毫秒数
     */
    public long getRestoreDuration() {
        return mRestoreDuration;
    }

    /**
     * 获取波浪弹回时间
     *
     * @param duration 毫秒数
     */
    public void setRestoreDuration(long duration) {
        mRestoreDuration = duration;
    }

    /**
     * 是否支持刷新
     *
     * @return true表示支持刷新，false则相反
     */
    public boolean isRefreshable() {
        return mIsRefreshable;
    }

    /**
     * 设置是否支持刷新
     *
     * @param isRefreshable true表示支持刷新，false则相反
     */
    public void setIsRefreshable(boolean isRefreshable) {
        mIsRefreshable = isRefreshable;
    }

    /**
     * 设置刷新监听器
     *
     * @param onRefreshListener 刷新监听器
     * @see OnRefreshListener
     */
    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        mOnRefreshListener = onRefreshListener;
    }

    /**
     * 界面刷新监听器
     *
     * @see #setOnRefreshListener(OnRefreshListener)
     * @see #finishRefresh(boolean)
     */
    public interface OnRefreshListener {

        /**
         * 获取刷新数据
         * 当获取完成后可调用{@link #finishRefresh(boolean)}完成刷新数据获取，并将水波恢复成初始状态，
         * 当需要在页面加载数据时
         */
        void onAcquireData();

        /**
         * 加载刷新数据
         * 获取到数据并调用{@link #finishRefresh(boolean)}弹回波浪后，在该方法中执行加载数据的操作
         */
        void onLoadData();
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        return new LayoutParams(lp);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    public static class LayoutParams extends LinearLayout.LayoutParams {

        /**
         * 子控件类型，可取{@link #TYPE_CHILD_HEADER},{@link #TYPE_CHILD_FOOTER}及{@link #TYPE_CHILD_NONE}中的一个
         */
        public int childType;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.WaveRefreshLayout_Layout);
            childType = a.getInteger(R.styleable.WaveRefreshLayout_Layout_childType, 0);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
            childType = TYPE_CHILD_NONE;
        }

        public LayoutParams(int width, int height, float weight) {
            super(width, height, weight);
            childType = TYPE_CHILD_NONE;
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