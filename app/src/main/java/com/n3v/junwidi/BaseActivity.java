package com.n3v.junwidi;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;


public class BaseActivity extends AppCompatActivity {


//    private LoadingDialog loadingDialog;
//
//    protected void showLoadingDialog(String message) {
//        if (loadingDialog == null) {
//            loadingDialog = new LoadingDialog(this);
//        }
//        loadingDialog.show(message, true, false);
//    }
//
//    protected void dismissLoadingDialog() {
//        if (loadingDialog != null) {
//            loadingDialog.dismiss();
//        }
//    }


    protected void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public static class ButtonWidget extends AppCompatButton {

        public ButtonWidget(Context context) {
            super(context);
        }

        public ButtonWidget(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public ButtonWidget(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        public void setEnabled(boolean enabled) {
            setAlpha(enabled ? 1 : 0.3f);
            super.setEnabled(enabled);
        }

    }
}
