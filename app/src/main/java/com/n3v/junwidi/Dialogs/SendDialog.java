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
import com.n3v.junwidi.Listener.MyDialogListener;
import com.n3v.junwidi.R;
import com.n3v.junwidi.Services.MyClientTask;

public class SendDialog extends Dialog {

    public final static int SEND_DLG_INIT = 1111;
    public final static int SEND_DLG_SENDING = 2222;
    public final static String SEND_DLG_WAITING_STR = "수락 대기중";

    Context mContext = null;

    private int receivers = 0;

    MyDialogListener myDialogListener = null;
    private int progress = 0;

    private Button okButton;
    private Button cancelButton;
    private ProgressBar progressBar;
    private TextView percentage;
    private TextView videoTitle;
    private TextView question;

    private String fileName = "";

    private int state = SEND_DLG_INIT;

    public SendDialog(Context context, final String fileName, MyDialogListener dialogListener) {
        super(context);
        this.mContext = context;
        this.fileName = fileName;
        this.myDialogListener = dialogListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_dialog);
        okButton = findViewById(R.id.send_dialog_btn_ok);
        cancelButton = findViewById(R.id.send_dialog_btn_cancel);
        progressBar = findViewById(R.id.send_dialog_progressbar);
        percentage = findViewById(R.id.send_dialog_progress_percent);
        videoTitle = findViewById(R.id.send_dialog_txt_file_title);
        question = findViewById(R.id.send_dialog_txt_question);
        progressBar.setVisibility(View.GONE);
        percentage.setVisibility(View.GONE);


    }

    private View.OnClickListener dialogClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

        }
    };

    public void setProgress(int progress) {
        progressBar.setProgress(progress);
        this.progress = progress;
        String str_Percent = Integer.toString(this.progress) + " / 100";
        percentage.setText(str_Percent);
        if (this.progress == 100) {
            finish();
        }
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
        Log.v("SendDialog", this.fileName);
        videoTitle.setText(this.fileName);
    }

    private void finish() {
        myDialogListener.onProgressFinished();
    }

    public void setSending() {
        question.setVisibility(View.GONE);
        okButton.setEnabled(false);
        percentage.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        okButton.setTextColor(Color.parseColor(Constants.BLOCKED_GRAY));
    }

    public void setWaiting() {
        question.setText(SEND_DLG_WAITING_STR);
        okButton.setEnabled(false);
        okButton.setTextColor(Color.parseColor(Constants.BLOCKED_GRAY));
    }

    public void setMyDialogListener(MyDialogListener myDialogListener) {
        this.myDialogListener = myDialogListener;
    }

    public void setReceivers(int receivers) {
        this.receivers = receivers;
    }

}
