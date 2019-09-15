package com.n3v.junwidi.Utils;

public class Constants {
    public static final int FILE_SERVICE_PORT = 8888; // File 전송에 사용할 PORT 번호
    public static final int CONTROL_WAITING_PORT = 8889; // Control message 전송에 사용할 PORT 번호
    public static final int CONTROL_SEND_PORT = 8891;
    public static final int FILE_TRANSFER_PORT = 8899;
    public static final int WAITING_PORT = 8890; // 대기중 사용하는 PORT 번호
    public static final int HANDSHAKE_TIMEOUT = 3000; // socket.receive 시 사용할 Timeout (단위 : ms)
    public static final int LONG_TIMEOUT = 10000; // socket.receive 시 사용할 Timeout (단위 : ms)
    public static final int COMMON_TIMEOUT = 5000;
    public static final int SHORT_TIMEOUT = 2000;

    public static final int FILE_BUFFER_SIZE = 512; // 파일 전송시 기본 버퍼 사이즈
    public static final int FILE_HEADER_SIZE = 20; // 파일 순서 번호 헤더 사이즈

    public static final int CONTROL_BUFFER_SIZE = 256; // 제어 메시지 버퍼 사이즈

    public static final String CONTROL_PREPARE = "PREPARE";
    public static final String CONTROL_PLAY = "PLAY";
    public static final String CONTROL_PAUSE = "PAUSE";
    public static final String CONTROL_RESUME = "RESUME";
    public static final String CONTROL_STOP = "STOP";
    public static final String CONTROL_SEEK = "SEEK";
    public static final String CONTROL_CANCEL = "CANCEL";

    public static final String CANCEL_WAITING = "CANCEL_HANDSHAKE";
    public static final String HANDSHAKE_SERVER_RECEIVE = "HANDSHAKE_SERVER_RECEIVE";
    public static final String TRANSFER_START = "TRSF_START";
    public static final String VIDEO_ALREADY_EXIST = "VIDEO_ALREADY_EXIST";
    public static final String RECEIVE_WAIT = "RCV_WAIT";
    public static final String RECEIVE_DENY = "RCV_DENY";
    public static final String SHOW_GUIDELINE = "SHOW_GUIDELINE";
    public static final String CHECK_GUIDELINE = "CHECK_GUIDELINE";
    public static final String READY_GUIDELINE = "READY_GUIDELINE";
    public static final String DELIMITER = "+=+";
    public static final String PREPARE_PLAY = "PREPARE_PLAY";
    public static final String PREPARE_RECEIVE = "PREPARE_RECEIVE";

    public static final String COLOR_BLOCKED_GRAY = "#AAAAAA";
    public static final String COLOR_OK_SKYBLUE = "#4095FB";
    public static final String COLOR_CANCEL_RED = "#FF0303";
}
