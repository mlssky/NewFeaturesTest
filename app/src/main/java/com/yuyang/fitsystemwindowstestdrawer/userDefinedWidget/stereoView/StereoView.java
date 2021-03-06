package com.yuyang.fitsystemwindowstestdrawer.userDefinedWidget.stereoView;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * 上下3D滚动效果
 * 缺陷：目前只支持最少三个子View
 *
 setStartScreen(int startScreen) ：设置第一页展示的页面 @param startScreen (0,getChildCount-1)

 setResistance(float resistance) ： 设置滑动阻力 @param resistance (0,...)

 setInterpolator(Interpolator mInterpolator) ： 设置滚动时interpolator插补器

 setAngle(float mAngle)：设置滚动时两个item的夹角度数 [0f,180f]

 setCan3D(boolean can3D) : 是否开启3D效果

 setItem(int itemId) : 跳转到指定的item @param itemId [0,getChildCount-1]

 toPre() : 上一页

 toNext() ： 下一页

 setiStereoListener(IStereoListener iStereoListener) : 上/下滑一页时回调
 */
public class StereoView extends ViewGroup {
    //可对外进行设置的参数
    private int mStartScreen = 1;//开始时的item位置
    private float resistance = 1.5f;//滑动阻力
    private Scroller mScroller;
    private float mAngle = 90;//两个item间的夹角
    private boolean isCan3D = true;//是否开启3D效果

    private Context mContext;
    private VelocityTracker mVelocityTracker;
    private Camera mCamera;
    private Matrix mMatrix;
    private float mDownY;//记录按下时的Y坐标
    private float mLastY;//纪录上次MotionEvent的Y坐标
    private int mWidth;//容器的宽度
    private int mHeight;//容器的高度
    private static final int standerSpeed = 2000;
    private static final int flingSpeed = 800;
//    private int addCount;//记录手离开屏幕后，需要新增的页面次数
    private int alreadyAdd = 0;//对滑动多页时的已经新增页面次数的记录
    private boolean isAdding = false;//fling时正在添加新页面，在绘制时不需要开启camera绘制效果，否则页面会有闪动
    private int mCurScreen = 1;//记录当前item
    private IStereoListener iStereoListener;
    private State mState = State.Normal;

    public enum State {
        Normal, ToPre, ToNext
    }

    public StereoView(Context context) {
        this(context, null);
    }

    public StereoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StereoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init(context);
    }

    private void init(Context context) {
        mCamera = new Camera();
        mMatrix = new Matrix();
        if (mScroller == null){
            mScroller = new Scroller(context);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();

        //滑动到设置的StartScreen位置
        scrollTo(0, mStartScreen * mHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childTop = 0;
        for (int i=0; i<getChildCount(); i++){
            View child = getChildAt(i);
            if (child.getVisibility() == View.VISIBLE){
                child.layout(0, childTop, child.getMeasuredWidth(), childTop+child.getMeasuredHeight());
                childTop += child.getMeasuredHeight();
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN){
            mDownY = ev.getY();
            this.onTouchEvent(ev);
        }
        super.dispatchTouchEvent(ev);
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!mScroller.isFinished()){
            return true;
        }
        float eventY = ev.getY();
        if (ev.getAction() == MotionEvent.ACTION_MOVE){
            float dexY = eventY - mDownY;
            if (Math.abs(dexY) > 10){
                return true;
            }
        }
        if (ev.getAction() == MotionEvent.ACTION_UP){
            float dexY = eventY - mDownY;
            if (Math.abs(dexY) > 10){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mVelocityTracker == null){
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        float y = event.getY();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()){
                    //当上一次滑动没有结束时，再次点击，强制滑动在点击位置结束
                    mScroller.setFinalY(mScroller.getCurrY());
                    mScroller.abortAnimation();
                    scrollTo(0, getScrollY());
                }
                mLastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(mDownY - y)/resistance >= mHeight){
                    break;
                }
                int realDelta = (int) (mLastY - y);
                mLastY = y;
                if (mScroller.isFinished()){
                    //因为要循环滚动
                    recycleMove(realDelta);
                }
                break;
            case MotionEvent.ACTION_UP:
                mVelocityTracker.computeCurrentVelocity(1000);
                float yVelocity = mVelocityTracker.getYVelocity();
                if (yVelocity > standerSpeed || (yVelocity>0 && Math.abs(y-mDownY) > mHeight/2) ) {//((getScrollY() + mHeight / 2) / mHeight < mStartScreen)
                    mState = State.ToPre;
                } else if (yVelocity < -standerSpeed || (yVelocity<0 && Math.abs(y-mDownY) > mHeight/2) ) {//((getScrollY() + mHeight / 2) / mHeight > mStartScreen)
                    //滑动的速度大于规定的速度，或者向下滑动时，下一页页面展现出的高度超过1/2。则设定状态为State.ToNext
                    mState = State.ToNext;
                } else {
                    mState = State.Normal;
                }
                //根据mState进行相应的变化
                changeByState();
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
        }
        return true;
    }

    @Override
    public void computeScroll() {
        //滑动没有结束时，进行的操作
        if (mScroller.computeScrollOffset()) {
            if (mState == State.ToPre) {
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY() + mHeight * alreadyAdd);
                if (getScrollY() < (mHeight)) {
                    isAdding = true;
                    addPre();
                    alreadyAdd++;
                }
            } else if (mState == State.ToNext) {
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY() - mHeight * alreadyAdd);
                if (getScrollY() > (mHeight)) {
                    isAdding = true;
                    addNext();
                    alreadyAdd++;
                }
            } else {
                //mState == State.Normal状态
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            }
            postInvalidate();
        }
        //滑动结束时相关用于计数变量复位
        if (mScroller.isFinished()) {
            alreadyAdd = 0;
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (!isAdding && isCan3D) {
            //当开启3D效果并且当前状态不属于 computeScroll中 addPre() 或者addNext()
            //如果不做这个判断，addPre() 或者addNext()时页面会进行闪动一下
            //我当时写的时候就被这个坑了，后来通过log判断，原来是computeScroll中的onlayout,和子Child的draw触发的顺序导致的。
            //知道原理的朋友希望可以告知下
            for (int i = 0; i < getChildCount(); i++) {
                drawScreen(canvas, i, getDrawingTime());
            }
        } else {
            isAdding = false;
            super.dispatchDraw(canvas);
        }
    }

    private void drawScreen(Canvas canvas, int i, long drawingTime) {
        int curScreenY = mHeight * i;
        //屏幕中不显示的部分不进行绘制
        if (getScrollY() + mHeight < curScreenY) {
            return;
        }
        if (curScreenY < getScrollY() - mHeight) {
            return;
        }
        float centerX = mWidth / 2;
        float centerY = (getScrollY() > curScreenY) ? curScreenY + mHeight : curScreenY;
        float degree = mAngle * (getScrollY() - curScreenY) / mHeight;
        if (degree > 90 || degree < -90) {
            return;
        }
        canvas.save();

        mCamera.save();
        mCamera.rotateX(degree);
        mCamera.getMatrix(mMatrix);
        mCamera.restore();

        mMatrix.preTranslate(-centerX, -centerY);
        mMatrix.postTranslate(centerX, centerY);
        canvas.concat(mMatrix);
        drawChild(canvas, getChildAt(i), drawingTime);
        canvas.restore();
    }

    private void changeByState() {
        alreadyAdd = 0;//重置滑动多页时的计数
        if (getScrollY() != mHeight) {
            switch (mState) {
                case Normal:
                    toNormalAction();
                    break;
                case ToPre:
                    toPreAction(1);
                    break;
                case ToNext:
                    toNextAction(1);
                    break;
            }
            invalidate();
        }
    }

    /**
     * mState = State.Normal 时进行的动作
     */
    private void toNormalAction() {
        int startY;
        int delta;
        int duration;
        mState = State.Normal;
        startY = getScrollY();
        delta = mHeight * mStartScreen - getScrollY();
        duration = (Math.abs(delta)) * 4;
        mScroller.startScroll(0, startY, 0, delta, duration);
    }

    /**
     * mState = State.ToPre 时进行的动作
     */
    private void toPreAction(int index) {
        int startY;
        int delta;
        int duration;
        mState = State.ToPre;
        addPre();//增加新的页面
        //计算松手后滑动的item个数
        //mScroller开始的坐标
        startY = getScrollY() + mHeight;
        setScrollY(startY);
        //mScroller移动的距离
        delta = -(startY - mStartScreen * mHeight) - (index - 1) * mHeight;
        duration = (Math.abs(delta)) * 3;
        mScroller.startScroll(0, startY, 0, delta, duration);
    }
    /**
     * mState = State.ToNext 时进行的动作
     */
    private void toNextAction(int index) {
        int startY;
        int delta;
        int duration;
        mState = State.ToNext;
        addNext();
        startY = getScrollY() - mHeight;
        setScrollY(startY);
        delta = mHeight * mStartScreen - startY + (index - 1) * mHeight;
        duration = (Math.abs(delta)) * 3;
        mScroller.startScroll(0, startY, 0, delta, duration);
    }

    /**
     * 实现循环滑动，需要不断的将滑出的子View移动至下一个划入的位置
     * @param delta
     */
    private void recycleMove(int delta) {
        delta = delta % mHeight;//取余
        delta = (int) (delta / resistance);
        if (Math.abs(delta) > mHeight / 4) {
            return;
        }
        scrollBy(0, delta);
        if (getScrollY()<0 && mStartScreen!=0){//向下一页滚动，超过一个子View的高度
            addPre();
            scrollBy(0, mHeight);//设置已经滚动了一个子View的高度，这样delta求余后就是接下来的滚动距离
        }else if (getScrollY() > (getChildCount() - 1) * mHeight) {//向上一次滚动超过下面子View的高度之和
            addNext();
            scrollBy(0, -mHeight);
        }
    }

    /**
     * 把第一个item移动到最后一个item位置
     */
    private void addNext() {
        mCurScreen = (mCurScreen + 1) % getChildCount();
        int childCount = getChildCount();
        View view = getChildAt(0);
        removeViewAt(0);
        addView(view, childCount - 1);
        if (iStereoListener != null) {
            iStereoListener.toNext(mCurScreen);
        }
    }

    /**
     * 把最后一个item移动到第一个item位置
     */
    private void addPre() {
        mCurScreen = ((mCurScreen - 1) + getChildCount()) % getChildCount();
        int childCount = getChildCount();
        View view = getChildAt(childCount - 1);
        removeViewAt(childCount - 1);
        addView(view, 0);
        if (iStereoListener != null) {
            iStereoListener.toPre(mCurScreen);
        }
    }

    public interface IStereoListener {
        //上滑一页时回调
        void toPre(int curScreen);
        //下滑一页时回调
        void toNext(int curScreen);
    }

    public void setiStereoListener(IStereoListener iStereoListener) {
        this.iStereoListener = iStereoListener;
    }

    /**
     * 设置第一页展示的页面
     *
     * @param startScreen (0,getChildCount-1)
     * @return
     */
    public StereoView setStartScreen(int startScreen) {
        if (startScreen <= 0 || startScreen >= (getChildCount() - 1)) {
            throw new IndexOutOfBoundsException("请输入规定范围内startScreen位置号");

        }
        this.mStartScreen = startScreen;
        this.mCurScreen = startScreen;
        return this;
    }

    /**
     * 设置移动阻力
     *
     * @param resistance (0,...)
     * @return
     */
    public StereoView setResistance(float resistance) {
        this.resistance = resistance;
        return this;
    }

    /**
     * 设置滚动时interpolator插补器
     *
     * @param mInterpolator
     * @return
     */
    public StereoView setInterpolator(Interpolator mInterpolator) {
        mScroller = new Scroller(mContext, mInterpolator);
        return this;
    }

    /**
     * 设置滚动时两个item的夹角度数
     * @param mAngle [0f,180f]
     * @return
     */
    public StereoView setAngle(float mAngle) {
        this.mAngle = 180f - mAngle;
        return this;
    }

    /**
     * 是否开启3D效果
     * @param can3D
     * @return
     */
    public StereoView setCan3D(boolean can3D) {
        isCan3D = can3D;
        return this;
    }

    /**
     * 跳转到指定的item
     *
     * @param itemId [0,getChildCount-1]
     * @return
     */
    public StereoView setItem(int itemId) {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        if (itemId < 0 || itemId > (getChildCount() - 1)) {
            throw new IndexOutOfBoundsException("请输入规定范围内item位置号");

        }
        if (itemId > mCurScreen) {
            toNextAction(itemId-mCurScreen);
        } else if (itemId < mCurScreen) {
            toPreAction(mCurScreen-itemId);
        }
        return this;
    }

    /**
     * 上一页
     * @return
     */
    public StereoView toPre() {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        toPreAction(1);
        return this;
    }

    /**
     * 下一页
     * @return
     */
    public StereoView toNext() {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        toNextAction(1);
        return this;
    }
}
