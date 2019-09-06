package com.n3v.junwidi.Listener;

public interface MyServerTaskListener {

    void progressUpdate(int progress);
    void onSendFinished();
    void onHandshaked();
    void onAllSendFinished();
    void onWaiting();
    void onCancelTransfer();
    void onNotify();
}
