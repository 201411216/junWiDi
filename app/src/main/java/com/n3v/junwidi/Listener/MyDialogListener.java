package com.n3v.junwidi.Listener;

public interface MyDialogListener {

    void onProgressFinished();

    void onRcvClickOK(int state);
    void onRcvClickCancel(int state);

    void onSendClickOK(int state);
    void onSendClickCancel(int state);

    void onAllProgressFinished();

}
