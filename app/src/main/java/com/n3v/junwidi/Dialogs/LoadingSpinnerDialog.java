package com.n3v.junwidi.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;

import com.n3v.junwidi.R;

public class LoadingSpinnerDialog extends Dialog {

    private Context mContext = null;

    private ProgressBar spinningView = null;

    public LoadingSpinnerDialog(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_spinner);
        this.setCanceledOnTouchOutside(false);
    }
}
