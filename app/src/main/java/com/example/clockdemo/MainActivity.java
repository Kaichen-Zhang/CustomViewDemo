package com.example.clockdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity{

    private ClockView mClockView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mClockView=(ClockView)findViewById(R.id.dv_clock);
        mClockView.setTime(Calendar.getInstance());


    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
    }
}
