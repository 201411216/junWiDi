package com.n3v.junwidi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.FrameLayout.LayoutParams;

public class GuideLineClient extends AppCompatActivity {

    int position = 2;
    public int W,H;
    public int videoX,videoY;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_line_client);
        ImageView ivID1 = findViewById(R.id.order11);
        ImageView ivID2 = findViewById(R.id.order22);
        if (position == 1) {
            ivID2.setVisibility(View.GONE);
        }
        if (position == 2) {
            ivID1.setVisibility(View.GONE);
        }

        ImageView ivGL = findViewById(R.id.GL);
        FrameLayout fL = findViewById(R.id.fL);
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        LayoutParams params = (LayoutParams) ivGL.getLayoutParams();
        LayoutParams params2 = (LayoutParams) fL.getLayoutParams();
        W = 100;
        H = 50;
        PxToMm(W, metrics);
        PxToMm(H, metrics);
        W = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 100, getResources().getDisplayMetrics());
        H = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 50, getResources().getDisplayMetrics());
        //비디오뷰 사이즈 결정
        params.height = H;
        params.width = W;
        params2.gravity = Gravity.BOTTOM;
        ivGL.setLayoutParams(params);
        fL.setLayoutParams(params2);
        ivGL.setX(-1080);

        Intent intent = new Intent(getApplicationContext(),PlayerHost.class);
        Bundle bundle = new Bundle();
        bundle.putInt("videoWidth",W);
        bundle.putInt("videoHeight",H);
        bundle.putInt("videoX",videoX);
        bundle.putInt("videoY",videoY);
        intent.putExtras(bundle);
    }

    public int PxToMm(int value, DisplayMetrics metrics) {
        return value * metrics.densityDpi;
    }


}