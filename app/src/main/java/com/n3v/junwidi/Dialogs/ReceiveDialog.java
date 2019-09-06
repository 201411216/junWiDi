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

public class ReceiveDialog extends Dialog {

    public static final int RCV_DLG_INIT = 1111;
    public static final int RCV_DLG_DOWNLOADING = 2222;
    public static final int RCV_DLG_VIDEO_ALREADY_EXISTS = 3333;
    public static final String RCV_DLG_VIDEO_ALREADY_EXISTS_STR = "영상이 이미 존재합니다";
    public static final String RCV_DLG_DEFAULT_QUESTION_STR = "파일을 다운로드 하시겠습니까?";
    public static final String RCV_DLG_DEFAULT_OK_BUTTON_STR = "다운로드";
    public static final String RCV_DLG_CHECK_OK_BUTTON_STR = "확인";

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
        this.mContext = context;
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
        okButton.setOnClickListener(dialogClickListner);
        cancelButton.setOnClickListener(dialogClickListner);
        initDialog();
        this.setCanceledOnTouchOutside(false);
    }

    public void initDialog(){
        state = RCV_DLG_INIT;
        question.setText(RCV_DLG_DEFAULT_QUESTION_STR);
        question.setVisibility(View.VISIBLE);
        videoTitle.setText("-");
        videoTitle.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);
        progressBar.setVisibility(View.GONE);
        percentage.setText("0 / 100");
        percentage.setVisibility(View.GONE);
        okButton.setTextColor(Color.parseColor(Constants.COLOR_OK_SKYBLUE));
        okButton.setText(RCV_DLG_DEFAULT_OK_BUTTON_STR);
        okButton.setEnabled(true);
        cancelButton.setTextColor(Color.parseColor(Constants.COLOR_CANCEL_RED));
        cancelButton.setEnabled(true);
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

    public void setFileName(String fileName) {
        this.fileName = fileName;
        Log.v("ReceiveDialog", this.fileName);
        videoTitle.setText(this.fileName);
    }

    public void setDownloading() {
        question.setVisibility(View.GONE);
        okButton.setEnabled(false);
        okButton.setTextColor(Color.parseColor(Constants.COLOR_BLOCKED_GRAY));
        cancelButton.setEnabled(true);
        cancelButton.setTextColor(Color.parseColor(Constants.COLOR_CANCEL_RED));
        percentage.setText("0 / 100");
        percentage.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);
        progressBar.setVisibility(View.VISIBLE);
    }

    public void setAlreadyExists() {
        state = RCV_DLG_VIDEO_ALREADY_EXISTS;
        cancelButton.setEnabled(false);
        okButton.setEnabled(true);
        cancelButton.setTextColor(Color.parseColor(Constants.COLOR_BLOCKED_GRAY));
        okButton.setText(RCV_DLG_CHECK_OK_BUTTON_STR);
        okButton.setTextColor(Color.parseColor(Constants.COLOR_OK_SKYBLUE));
        percentage.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        question.setText(RCV_DLG_VIDEO_ALREADY_EXISTS_STR);
        question.setVisibility(View.VISIBLE);
        videoTitle.setVisibility(View.VISIBLE);
    }

    private View.OnClickListener dialogClickListner = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.equals(okButton)) {
                myDialogListener.onRcvClickOK(state);
                state = RCV_DLG_DOWNLOADING;
                setDownloading();
            } else if (view.equals(cancelButton)) {
                myDialogListener.onRcvClickCancel(state);
            }
        }
    };

    private void finish() {
        myDialogListener.onProgressFinished();
    }

    public void setMyDialogListener(MyDialogListener myDialogListener) {
        this.myDialogListener = myDialogListener;
    }
}
