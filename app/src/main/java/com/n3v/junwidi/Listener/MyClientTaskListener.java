package com.n3v.junwidi.Listener;

public interface MyClientTaskListener {

    void onEndWait();
    void progressUpdate(int progress);
    void onHandshaked();
    void setFile(String fileName, long fileSize);
    void onReceiveFinished();
}
