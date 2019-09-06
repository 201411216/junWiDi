package com.n3v.junwidi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.FrameLayout.LayoutParams;


public class GuideLineHost extends AppCompatActivity {


    int aX;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_line_host);
        ImageView ivID = findViewById(R.id.order3);
        ImageView ivGL = findViewById(R.id.GL);
        FrameLayout fL = findViewById(R.id.fL);
        Button button = findViewById(R.id.button);
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager)getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        LayoutParams params = (LayoutParams)ivGL.getLayoutParams();
        LayoutParams params2=(LayoutParams)fL.getLayoutParams();
        LayoutParams params3=(LayoutParams)button.getLayoutParams();
        int ww = 100;
        int hh = 50;
        PxToMm(ww, metrics);
        PxToMm(hh, metrics);
        ww = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 100, getResources().getDisplayMetrics());
        hh = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 100, getResources().getDisplayMetrics());
        //비디오뷰 사이즈 결정
        params.height = hh;
        params.width = ww;
        params2.gravity=Gravity.BOTTOM;
        params3.gravity=Gravity.TOP;
        ivGL.setLayoutParams(params);
        fL.setLayoutParams(params2);
        button.setLayoutParams(params3);
    }
    public int PxToMm(int value, DisplayMetrics metrics) {
        return value * metrics.densityDpi;
    }


}
