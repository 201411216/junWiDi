package com.n3v.junwidi;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


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

}
