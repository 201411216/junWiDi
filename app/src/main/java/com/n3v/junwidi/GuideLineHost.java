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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.FrameLayout.LayoutParams;


public class GuideLineHost extends AppCompatActivity {

    public int W;
    public int H;
    public int videoX;
    public int videoY;

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

        W = 100;
        H = 50;
        PxToMm(W, metrics);
        PxToMm(H, metrics);
        W = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, W, getResources().getDisplayMetrics());
        H = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, H, getResources().getDisplayMetrics());
        //비디오뷰 사이즈 결정
        params.width = W;
        params.height = H;
        params2.gravity=Gravity.BOTTOM;

        ivGL.setLayoutParams(params);
        fL.setLayoutParams(params2);


        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(),PlayerHost.class);
                Bundle bundle = new Bundle();
                bundle.putInt("videoWidth",W);
                bundle.putInt("videoHeight",H);
                bundle.putInt("videoX",videoX);
                bundle.putInt("videoY",videoY);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

    }
    public int PxToMm(int value, DisplayMetrics metrics) {
        return value * metrics.densityDpi;
    }



}
