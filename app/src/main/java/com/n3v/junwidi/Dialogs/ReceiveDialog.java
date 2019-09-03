package com.n3v.junwidi.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.n3v.junwidi.Listener.MyDialogListener;
import com.n3v.junwidi.R;

public class ReceiveDialog extends Dialog {

    MyDialogListener myDialogListener;
    int progress = 0;

    Context mContext;

    String fileName;

    private Button okButton;
    private Button cancelButton;
    private ProgressBar progressBar;
    private TextView percentage;
    private TextView videoTitle;


    public ReceiveDialog(Context context) {
        super(context);
        mContext = context;
    }

    public ReceiveDialog(Context context, String fileName) {
        super(context);
        mContext = context;
        this.fileName = fileName;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.receive_dialog);
        okButton = findViewById(R.id.rcv_dialog_btn_ok);
    }

}
