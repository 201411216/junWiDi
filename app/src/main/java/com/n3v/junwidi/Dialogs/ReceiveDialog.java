package com.n3v.junwidi.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.n3v.junwidi.Listener.MyDialogListener;
import com.n3v.junwidi.R;

import java.net.InetAddress;

public class ReceiveDialog extends Dialog {

    MyDialogListener myDialogListener;
    int progress = 0;

    Context mContext;

    String fileName;

    String host_addr;

    private Button okButton;
    private Button cancelButton;
    private ProgressBar progressBar;
    private TextView percentage;
    private TextView videoTitle;

    private RCV_DIALOG_STATE state = RCV_DIALOG_STATE.RCV_DIALOG_INIT;

    private enum RCV_DIALOG_STATE {
        RCV_DIALOG_INIT, RCV_DIALOG_DOWNLOADING, RCV_DIALOG_CANCEL
    }


    public ReceiveDialog(Context context) {
        super(context);
        mContext = context;
    }

    public ReceiveDialog(Context context, String fileName, String host_addr) {
        super(context);
        mContext = context;
        this.fileName = fileName;
        this.host_addr = host_addr;
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
        progressBar.setVisibility(View.GONE);
        percentage.setVisibility(View.GONE);
    }

    public void setProgress(int progress) {
        progressBar.setProgress(progress);
    }

    private View.OnClickListener dialogClickListner = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.equals(okButton)) {
                state = RCV_DIALOG_STATE.RCV_DIALOG_DOWNLOADING;
            } else if (view.equals(cancelButton)) {
                state = RCV_DIALOG_STATE.RCV_DIALOG_CANCEL;
                finish();
            }
        }
    };

    private void finish(){
        this.dismiss();
    }
}
