package com.n3v.junwidi;

public class Constants {
    public static final int FILE_SERVICE_PORT = 8888; // File 전송에 사용할 PORT 번호
    public static final int CONTROL_SERVICE_PORT = 8889; // Control message 전송에 사용할 PORT 번호
    public static final int HANDSHAKE_TIMEOUT = 1000; // socket.receive 시 사용할 Timeout (단위 : ms)
    public static final int LONG_TIMEOUT = 10000; // socket.receive 시 사용할 Timeout (단위 : ms)
    public static final int COMMON_TIMEOUT = 5000;

    public static final int FILE_BUFFER_SIZE = 512; // 파일 전송시 기본 버퍼 사이즈
    public static final int FILE_HEADER_SIZE = 20; // 파일 순서 번호 헤더 사이즈
}
