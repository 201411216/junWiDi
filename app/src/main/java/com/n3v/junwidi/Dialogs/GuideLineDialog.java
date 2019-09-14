package com.n3v.junwidi.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.n3v.junwidi.Datas.DeviceInfo;
import com.n3v.junwidi.Listener.MyGuidelineDialogListener;
import com.n3v.junwidi.R;

public class GuideLineDialog extends Dialog {

    public static final int GLD_HOST = 1029384;
    public static final int GLD_CLIENT = 1029385;

    private Context mContext = null;

    public int W;
    public int H;
    public int videoX;
    public int videoY;
    public DisplayMetrics displayMetrics = null;
    public DeviceInfo processedMyDeviceInfo = null;

    public MyGuidelineDialogListener mgdl = null;

    private int GLD_ACT_MODE = GLD_CLIENT;

    private ImageView ivID1 = null;
    private ImageView ivID2 = null;
    private ImageView ivID3 = null;
    private ImageView ivGL = null;
    private FrameLayout fL = null;
    private Button button = null;

    public GuideLineDialog(Context context, DisplayMetrics dm, int mode, MyGuidelineDialogListener mgdl) {
        super(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        mContext = context;
        displayMetrics = dm;
        GLD_ACT_MODE = mode;
        this.mgdl = mgdl;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guideline_dialog);
        ivID1 = findViewById(R.id.order1);
        ivID2 = findViewById(R.id.order2);
        ivID3 = findViewById(R.id.order3);
        ivGL = findViewById(R.id.GL);
        fL = findViewById(R.id.fL);
        button.setAlpha(1.0f);
        button = findViewById(R.id.button);
        if (GLD_ACT_MODE == GLD_HOST) {
            button.setVisibility(View.GONE);
            button.setEnabled(false);
            button.setAlpha(0.3f);
        } else {
            button.setVisibility(View.GONE);
            button.setEnabled(false);
            button.setAlpha(0.3f);
        }
        button.setEnabled(false);
        button.setAlpha(0.3f);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mgdl.onClickOkButton();
            }
        });

        initDialog();
    }
   

    public void initDialog(){
        ivID1.setVisibility(View.GONE);
        ivID2.setVisibility(View.GONE);
        ivID3.setVisibility(View.GONE);
    }

    public void setProcessedMyDeviceInfo(DeviceInfo processedMyDeviceInfo) {
        initDialog();
        this.processedMyDeviceInfo = processedMyDeviceInfo;
        if (this.processedMyDeviceInfo.getPosition() == 1) {
            ivID1.setVisibility(View.VISIBLE);
        } else if (this.processedMyDeviceInfo.getPosition() == 2) {
            ivID2.setVisibility(View.VISIBLE);
        } else if (this.processedMyDeviceInfo.getPosition() == 3) {
            ivID3.setVisibility(View.VISIBLE);
        }

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)ivGL.getLayoutParams();
        FrameLayout.LayoutParams params2 = (FrameLayout.LayoutParams)fL.getLayoutParams();

        W = processedMyDeviceInfo.getMm_videoview_width();
        H = processedMyDeviceInfo.getMm_videoview_height();

        Log.v("GuideLineDialog", "W = " + W + " / H = " + H);

        W = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, W, displayMetrics);
        H = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, H, displayMetrics);
        //비디오뷰 사이즈 결정

        int X = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, processedMyDeviceInfo.getSetXValue(), displayMetrics);

        ivGL.getLayoutParams().width = W;
        ivGL.getLayoutParams().height = H;
        ivGL.setX(X);
        ((FrameLayout.LayoutParams) fL.getLayoutParams()).gravity = Gravity.BOTTOM;
        ivGL.requestLayout();
        fL.requestLayout();

        //params.width = W;
        //params.height = H;
        //params2.gravity= Gravity.BOTTOM;

        //ivGL.setLayoutParams(params);
        //fL.setLayoutParams(params2);
    }



    public void onClientsReady() {
        button.setEnabled(true);
        button.setAlpha(1f);
        button.setVisibility(View.VISIBLE);
    }

}
