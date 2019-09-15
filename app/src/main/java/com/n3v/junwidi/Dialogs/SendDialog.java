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

import com.n3v.junwidi.Utils.Constants;
import com.n3v.junwidi.Listener.MyDialogListener;
import com.n3v.junwidi.R;

public class SendDialog extends Dialog {

    public static final int SEND_DLG_INIT = 1111;
    public static final int SEND_DLG_SENDING = 2222;
    public static final int SEND_DLG_WAITING = 3333;
    public static final String SEND_DLG_QUESTION_SEND_STR = "영상을 전송하시겠습니까?";
    public static final String SEND_DLG_WAITING_STR = "수락 대기중";

    Context mContext = null;

    private int receivers = 0;
    private int received = 0;

    private String receiver_Str = "";

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
        okButton.setOnClickListener(dialogClickListener);
        cancelButton.setOnClickListener(dialogClickListener);
        initDialog();
        this.setCanceledOnTouchOutside(false);
    }

    @Override
    public void cancel(){
        initDialog();
        super.cancel();
    }

    public void initDialog(){
        progress = 0;
        fileName = "";
        state = SEND_DLG_INIT;
        progressBar.setProgress(progress);
        videoTitle.setText(fileName);
        question.setText(SEND_DLG_QUESTION_SEND_STR);
        String str_Percent = this.progress + " / 100";
        percentage.setText(str_Percent);
        progressBar.setVisibility(View.GONE);
        percentage.setVisibility(View.GONE);
        question.setVisibility(View.VISIBLE);
        okButton.setTextColor(Color.parseColor(Constants.COLOR_OK_SKYBLUE));
        cancelButton.setTextColor(Color.parseColor(Constants.COLOR_CANCEL_RED));
        okButton.setEnabled(true);
        okButton.setAlpha(1f);
    }

    private View.OnClickListener dialogClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.equals(okButton)) {
                myDialogListener.onSendClickOK(state);
            }
            if (view.equals(cancelButton)) {
                myDialogListener.onSendClickCancel(state);
            }
        }
    };

    public void setProgress(int progress) {
        progressBar.setProgress(progress);
        this.progress = progress;
        String str_Percent = this.progress + " / 100";
        percentage.setText(str_Percent);
        //if (this.progress == 100) {
        //}
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
        Log.v("SendDialog", this.fileName);
        videoTitle.setText(this.fileName);
    }

    private void finish() {
        myDialogListener.onAllProgressFinished();
    }

    public void setSending() {
        state = SEND_DLG_SENDING;
        question.setVisibility(View.GONE);
        okButton.setEnabled(false);
        //okButton.setAlpha(0.3f);
        progress = 0;
        progressBar.setProgress(progress);
        String str_Percent = this.progress + " / 100";
        percentage.setText(str_Percent);
        percentage.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        okButton.setTextColor(Color.parseColor(Constants.COLOR_BLOCKED_GRAY));

    }

    public void setWaiting() {
        state = SEND_DLG_WAITING;
        question.setText(SEND_DLG_WAITING_STR);
        progressBar.setVisibility(View.VISIBLE);
        percentage.setText(receiver_Str);
        okButton.setEnabled(false);
        okButton.setAlpha(0.3f);
        okButton.setTextColor(Color.parseColor(Constants.COLOR_BLOCKED_GRAY));
    }

    public void setReceivers(int receivers) {
        this.receivers = receivers;
    }

    public void setReceiver_Str(String receiver_Str) {
        this.receiver_Str = receiver_Str;
    }

    public void sendCompleteOne() {
        received++;
        progress = 0;
        progressBar.setProgress(0);
        this.setWaiting();
    }
}
