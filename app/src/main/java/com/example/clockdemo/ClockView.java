package com.example.clockdemo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;

//这个函数主要利用自定义控件实现时钟显示当前时间

public class ClockView extends View {

    //借此参数推算角度
    private static final int MINUTES_IN_A_HOUR=60;
    private static final int DEGREES_OF_A_LAP=360;
    private static final int HOURS_OF_A_LAP=12;

    //边框高度 颜色
    private int mBorderWidth;
    private int mBorderColor;

    //数字大小 颜色
    private int mNumSize;
    private int mNumColor;

    //周边小点 颜色
    private int mPointColor;

    //时针分针秒针的颜色
    private int mHourColor;
    private int mMinuteColor;
    private int mSecondColor;

    //画笔
    private Paint mPaint;

    //刻度
    private Rect mBound;

    //自定义View长宽
    private int mHeight;
    private int mWidth;

    //控制变量 并发保证原子性
    private AtomicBoolean isShow;

    //时间
    private Calendar mCalendar;

    //开辟Background线程每隔1秒重新绘制 不阻塞UI
    private Handler mHandler = new Handler();
    Runnable mRunnable=new Runnable() {
        @Override
        public void run() {
            Log.d("test", "绘制一次");
            if (isShow.get()) {
                invalidate();
            }
            mHandler.postDelayed(this,1000);
        }
    };

    //构造函数
    public ClockView(Context context) {
        this(context, null);
    }

    public ClockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
        mHandler.postDelayed(mRunnable,0);
    }

    //初始化
    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ClockView, defStyleAttr, 0);
        for (int i = 0; i < array.getIndexCount(); i++) {
            int attr = array.getIndex(i);
            switch (attr) {
                //边框高度
                case R.styleable.ClockView_border_width:
                    mBorderWidth = array.getDimensionPixelSize(attr, (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
                    break;
                //边框颜色
                case R.styleable.ClockView_border_color:
                    mBorderColor = array.getColor(attr, Color.BLACK);
                    break;
                //数字颜色
                case R.styleable.ClockView_num_color:
                    mNumColor = array.getColor(attr, Color.BLACK);
                    break;
                //数字字号
                case R.styleable.ClockView_num_size:
                    mNumSize = array.getDimensionPixelSize(attr, (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics()));
                    break;
                //周围小点颜色
                case R.styleable.ClockView_point_color:
                    mPointColor = array.getColor(attr, Color.BLACK);
                    break;
                //时针颜色
                case R.styleable.ClockView_hour_color:
                    mHourColor = array.getColor(attr, Color.BLACK);
                    break;
                //分针颜色
                case R.styleable.ClockView_minute_color:
                    mMinuteColor = array.getColor(attr, Color.BLACK);
                    break;
                //秒针颜色
                case R.styleable.ClockView_second_color:
                    mSecondColor = array.getColor(attr, Color.BLACK);
                    break;
            }
        }
        array.recycle();
        isShow = new AtomicBoolean(true);
        mCalendar = Calendar.getInstance();
        mPaint = new Paint();
        mBound = new Rect();
    }


    //测量计算组件大小
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //获取控件测量模式及大小
        int widthMode=MeasureSpec.getMode(widthMeasureSpec);
        int widthSize=MeasureSpec.getSize(widthMeasureSpec);
        int heightMode=MeasureSpec.getMode(heightMeasureSpec);
        int heightSize=MeasureSpec.getSize(heightMeasureSpec);

        //根据模式测算距离
        if (widthMode == MeasureSpec.EXACTLY) {
            mWidth = widthSize;
        } else {
            int desire = getPaddingLeft() + getPaddingRight() + (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics());
            mWidth = Math.min(desire, widthSize);
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            mHeight = heightSize;
        } else {
            int desire = getPaddingTop() + getPaddingBottom() + (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics());
            mHeight = Math.min(desire, heightSize);
        }

        //为确保不会越过控件边界 取长宽中较小值
        mWidth = Math.min(mWidth, mHeight);
        setMeasuredDimension(mWidth, mWidth);
    }

    //绘制过程
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //边框大小
        mBorderWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        mBound.left = getPaddingLeft();
        mBound.right = mWidth - getPaddingRight();
        mBound.top = getPaddingTop();
        mBound.bottom = mHeight - getPaddingBottom();

        //圆形的位置
        final int cx, cy, width;
        cx = getPaddingLeft() + (getMeasuredWidth() - getPaddingLeft() - getPaddingRight()) / 2;
        cy = getPaddingTop() + (getMeasuredHeight() - getPaddingTop() - getPaddingBottom()) / 2;
        width = Math.min(getWidth() / 2, getHeight() / 2);

        //消除锯齿
        mPaint.setAntiAlias(true);

        //绘制边界
        mPaint.setColor(mBorderColor);
        if (mBorderColor == 0) {
            mPaint.setColor(Color.BLACK);
        }
        canvas.drawCircle(cx, cy, width, mPaint);

        //绘制时针 分针 秒针
        mPaint.setColor(Color.WHITE);
        canvas.drawCircle(cx, cy, width - mBorderWidth, mPaint);

        mPaint.setColor(Color.RED);
        canvas.drawCircle(cx, cy, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, getResources().getDisplayMetrics()), mPaint);

        mPaint.setColor(mPointColor);
        if (mPointColor == 0) {
            mPaint.setColor(Color.BLACK);
        }

        //保存状态
        canvas.save();

        //绘制时钟上刻度
        for (int i = 0; i < 60; i++) {
            if (i % 5 == 0) {
                canvas.drawRect(cx - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()),
                        getPaddingTop() + mBorderWidth + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics()),
                        cx + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()),
                        getPaddingTop() + mBorderWidth + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics()), mPaint);
            } else {
                canvas.drawRect(cx - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()),
                        getPaddingTop() + mBorderWidth + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics()),
                        cx + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()),
                        getPaddingTop() + mBorderWidth + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics()), mPaint);
            }
            canvas.rotate(6, cx, cy);
        }

        //回到上一次状态
        canvas.restore();

        mPaint.setColor(mNumColor);
        if (mNumColor == 0) {
            mPaint.setColor(Color.BLACK);
        }

        //绘制时钟上文字
        mPaint.setTextSize(mNumSize);
        if (mNumSize == 0) {
            mPaint.setTextSize((int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics()));
        }
        mPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, getResources().getDisplayMetrics()));
        String[] strs = new String[]{"12", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"};//绘制数字1-12  (数字角度不对  可以进行相关的处理)
        Rect rect = new Rect();
        canvas.save();
        for (int i = 0; i < 12; i++) {//绘制12次  每次旋转30度
            mPaint.getTextBounds(strs[i], 0, strs[i].length(), rect);
            canvas.drawText(strs[i], cx - rect.width() / 2,
                    getPaddingTop() + mBorderWidth + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18, getResources().getDisplayMetrics()) + rect.height(), mPaint);
            canvas.rotate(30, cx, cy);
        }
        canvas.restore();

        //当满60时 需要相应进位
        int hour = mCalendar.get(Calendar.HOUR);
        int minute = mCalendar.get(Calendar.MINUTE);
        int second = mCalendar.get(Calendar.SECOND) + 1;
        if (second == 60) {
            minute += 1;
            second = 0;
        }
        if (minute == 60){
            hour += 1;
            minute = 0;
        }
        if (hour == 12){
            hour = 0;
        }

        //设置时间
        mCalendar.set(Calendar.SECOND, second);
        mCalendar.set(Calendar.MINUTE, minute);
        mCalendar.set(Calendar.HOUR, hour);

        //测算角度
        float hourDegree =  DEGREES_OF_A_LAP * hour / HOURS_OF_A_LAP + DEGREES_OF_A_LAP / HOURS_OF_A_LAP * minute / MINUTES_IN_A_HOUR;
        float minuteDegree = DEGREES_OF_A_LAP * minute / MINUTES_IN_A_HOUR + DEGREES_OF_A_LAP / MINUTES_IN_A_HOUR * second / MINUTES_IN_A_HOUR;
        float secondDegree = DEGREES_OF_A_LAP * second / MINUTES_IN_A_HOUR;

        mPaint.setColor(mHourColor);
        if (mHourColor == 0) {
            mPaint.setColor(Color.BLACK);
        }
        canvas.save();
        canvas.rotate(hourDegree, cx, cy);
        canvas.drawRect(cx - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics()),
                getPaddingTop() + mBorderWidth + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics()) + rect.width(),
                cx + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics()),
                cy, mPaint);
        canvas.restore();
        mPaint.setColor(mMinuteColor);
        if (mMinuteColor == 0) {
            mPaint.setColor(Color.BLACK);
        }
        canvas.save();
        canvas.rotate(minuteDegree, cx, cy);
        canvas.drawRect(cx - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()),
                getPaddingTop() + mBorderWidth + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, getResources().getDisplayMetrics()),
                cx + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()),
                cy, mPaint);
        canvas.restore();

        mPaint.setColor(mSecondColor);
        if (mSecondColor == 0) {
            mPaint.setColor(Color.BLACK);
        }
        canvas.save();
        mPaint.setColor(Color.RED);
        canvas.rotate(secondDegree, cx, cy);
        canvas.drawRect(cx - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()),
                getPaddingTop() + mBorderWidth + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, getResources().getDisplayMetrics()),
                cx + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()),
                cy, mPaint);
        canvas.restore();

        mPaint.setColor(Color.RED);
        canvas.drawCircle(cx, cy, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, getResources().getDisplayMetrics()), mPaint);
    }

    //当Activity结束时 停止该时钟
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isShow.set(false);
        mHandler.removeCallbacks(mRunnable);
    }

    //设置当前时间
    public void setTime(Calendar calendar) {
        mCalendar = calendar;
    }

}
