package com.example.clockdemo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.jar.Attributes;

public class ClockView extends View {

    private int mBorderWidth;
    private int mBorderColor;

    private int mNumSize;
    private int mNumColor;

    private int mPointColor;

    private int mHourColor;
    private int mMinuteColor;
    private int mSecondColor;

    private Paint mPaint;

    private Rect mBound;

    private int mHeight;
    private int mWidth;

    private boolean isShow;

    private Calendar mCalendar;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            invalidate();
        }
    };

    public ClockView(Context context) {
        this(context, null);
    }

    public ClockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ClockView, defStyleAttr, 0);
        for (int i = 0; i < array.getIndexCount(); i++) {//用getIndexCount   减少循环次数，提高性能   用.length也不能执行所有的case情况
            int attr = array.getIndex(i);
            switch (attr) {
                case R.styleable.ClockView_border_width://边框宽度
                    mBorderWidth = array.getDimensionPixelSize(attr, (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
                    break;
                case R.styleable.ClockView_border_color://边框颜色
                    mBorderColor = array.getColor(attr, Color.BLACK);
                    break;
                case R.styleable.ClockView_num_color://数字颜色
                    mNumColor = array.getColor(attr, Color.BLACK);
                    break;
                case R.styleable.ClockView_num_size://数字字号
                    mNumSize = array.getDimensionPixelSize(attr, (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics()));
                    break;
                case R.styleable.ClockView_point_color://周围小点颜色
                    mPointColor = array.getColor(attr, Color.BLACK);
                    break;
                case R.styleable.ClockView_hour_color://时针颜色
                    mHourColor = array.getColor(attr, Color.BLACK);
                    break;
                case R.styleable.ClockView_minute_color://分针颜色
                    mMinuteColor = array.getColor(attr, Color.BLACK);
                    break;
                case R.styleable.ClockView_second_color://秒针颜色
                    mSecondColor = array.getColor(attr, Color.BLACK);
                    break;
            }
        }
        array.recycle();
        isShow = true;
        mCalendar = Calendar.getInstance();
        mPaint = new Paint();
        mBound = new Rect();
        Timer timer = new Timer("绘制线程");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d("test", "绘制一次");
                if (isShow) {
                    mHandler.sendEmptyMessage(1);
                }
            }
        }, 0, 1000);
    }



    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode=MeasureSpec.getMode(widthMeasureSpec);
        int widthSize=MeasureSpec.getSize(widthMeasureSpec);
        int heightMode=MeasureSpec.getMode(heightMeasureSpec);
        int heightSize=MeasureSpec.getSize(heightMeasureSpec);

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

        mWidth = Math.min(mWidth, mHeight);
        setMeasuredDimension(mWidth, mWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mBorderWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());

        mBound.left = getPaddingLeft();
        mBound.right = mWidth - getPaddingRight();
        mBound.top = getPaddingTop();
        mBound.bottom = mHeight - getPaddingBottom();

        final int cx, cy, width;
        cx = getPaddingLeft() + (getMeasuredWidth() - getPaddingLeft() - getPaddingRight()) / 2;
        cy = getPaddingTop() + (getMeasuredHeight() - getPaddingTop() - getPaddingBottom()) / 2;
        width = Math.min(getWidth() / 2, getHeight() / 2);

        mPaint.setAntiAlias(true);
        mPaint.setColor(mBorderColor);
        if (mBorderColor == 0) {
            mPaint.setColor(Color.BLACK);
        }
        canvas.drawCircle(cx, cy, width, mPaint);

        mPaint.setColor(Color.WHITE);
        canvas.drawCircle(cx, cy, width - mBorderWidth, mPaint);

        mPaint.setColor(Color.RED);
        canvas.drawCircle(cx, cy, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, getResources().getDisplayMetrics()), mPaint);

        mPaint.setColor(mPointColor);
        if (mPointColor == 0) {
            mPaint.setColor(Color.BLACK);
        }

        canvas.save();

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
        canvas.restore();

        mPaint.setColor(mNumColor);
        if (mNumColor == 0) {
            mPaint.setColor(Color.BLACK);
        }
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

        int hour = mCalendar.get(Calendar.HOUR);//HOUR    进制为12小时   HOUR_OF_DAY  为24小时
        int minute = mCalendar.get(Calendar.MINUTE);//分钟
        int second = mCalendar.get(Calendar.SECOND) + 1;//秒数
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
        mCalendar.set(Calendar.SECOND, second);
        mCalendar.set(Calendar.MINUTE, minute);
        mCalendar.set(Calendar.HOUR, hour);
        float hourDegree = 360 * hour / 12 + 360 / 12 * minute / 60;
        float minuteDegree = 360 * minute / 60 + 360 / 60 * second / 60;
        float secondDegree = 360 * second / 60;

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

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isShow = false;
    }

    public void setTime(Calendar calendar) {
        mCalendar = calendar;
    }

}
