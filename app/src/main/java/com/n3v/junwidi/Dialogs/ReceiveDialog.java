package com.n3v.junwidi.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.n3v.junwidi.Constants;
import com.n3v.junwidi.DeviceInfo;
import com.n3v.junwidi.Listener.MyDialogListener;
import com.n3v.junwidi.R;

import java.net.InetAddress;

public class ReceiveDialog extends Dialog {

    public static int RCV_DLG_INIT = 1111;
    public static int RCV_DLG_DOWNLOADING = 2222;

    MyDialogListener myDialogListener = null;
    private int progress = 0;

    Context mContext = null;

    private String fileName = "";

    private Button okButton;
    private Button cancelButton;
    private ProgressBar progressBar;
    private TextView percentage;
    private TextView videoTitle;
    private TextView question;

    private int state = RCV_DLG_INIT;


    public ReceiveDialog(Context context, final String fileName, MyDialogListener dialogListener) {
        super(context);
        mContext = context;
        this.fileName = fileName;
        this.myDialogListener = dialogListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.receive_dialog);
        okButton = findViewById(R.id.rcv_dialog_btn_ok);
        cancelButton = findViewById(R.id.rcv_dialog_btn_cancel);
        progressBar = findViewById(R.id.rcv_dialog_progressbar);
        percentage = findViewById(R.id.rcv_dialog_progress_percent);
        videoTitle = findViewById(R.id.rcv_dialog_txt_file_title);
        question = findViewById(R.id.rcv_dialog_txt_question);
        progressBar.setVisibility(View.GONE);
        percentage.setVisibility(View.GONE);
        okButton.setOnClickListener(dialogClickListner);
        cancelButton.setOnClickListener(dialogClickListner);
        this.setCanceledOnTouchOutside(false);
    }

    public void setProgress(int progress) {
        progressBar.setProgress(progress);
        this.progress = progress;
        String str_Percent = Integer.toString(this.progress) + " / 100";
        percentage.setText(str_Percent);
        if (this.progress == 100) {
            finish();
        }
    }

    public void setDownloading() {
        question.setVisibility(View.GONE);
        okButton.setEnabled(false);
        percentage.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        okButton.setTextColor(Color.parseColor(Constants.BLOCKED_GRAY));
    }

    private View.OnClickListener dialogClickListner = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.equals(okButton)) {
                myDialogListener.onClickOK(state);
                state = RCV_DLG_DOWNLOADING;
                setDownloading();
            } else if (view.equals(cancelButton)) {
                myDialogListener.onClickCancel(state);
            }
        }
    };

    private void finish() {
        myDialogListener.onProgressFinished();
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
        Log.v("ReceiveDialog", this.fileName);
        //videoTitle.setText(this.fileName);
    }

    public void setMyDialogListener(MyDialogListener myDialogListener) {
        this.myDialogListener = myDialogListener;
    }
}
