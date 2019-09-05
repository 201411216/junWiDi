package com.n3v.junwidi.BroadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

// 플레이어 제어를 위한 6가지 상태변화
public class PlayerBroadCastReceiver extends BroadcastReceiver {

    public static final String PLAYER_PREPARE_ACTION = "tt.player.PREPARE_ACTION"; // 대기 상태 : 재생할 영상 확인, 영상 일치 여부 확인, 네트워크 지연 확인 등
    public static final String PLAYER_PLAY_ACTION = "tt.player.PLAY_ACTION"; // 최초 시작
    public static final String PLAYER_PAUSE_ACTION = "tt.player.PAUSE_ACTION"; // 일시 정지
    public static final String PLAYER_RESUME_ACTION = "tt.player.RESUME_ACTION"; // 재시작
    public static final String PLAYER_STOP_ACTION = "tt.player.STOP_ACTION"; // 정지
    public static final String PLAYER_MOVE_ACTION = "tt.player.MOVE_ACTION"; // 이동(지정 시간으로)

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        if (PLAYER_PREPARE_ACTION.equals(action)) {

        } else if (PLAYER_PLAY_ACTION.equals(action)) {

        } else if (PLAYER_PAUSE_ACTION.equals(action)) {

        } else if (PLAYER_RESUME_ACTION.equals(action)) {

        } else if (PLAYER_STOP_ACTION.equals(action)) {

        } else if (PLAYER_MOVE_ACTION.equals(action)) {

        }

    }

    public static IntentFilter getIntentFilter() {

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PLAYER_PREPARE_ACTION);
        intentFilter.addAction(PLAYER_PLAY_ACTION);
        intentFilter.addAction(PLAYER_PAUSE_ACTION);
        intentFilter.addAction(PLAYER_RESUME_ACTION);
        intentFilter.addAction(PLAYER_STOP_ACTION);
        intentFilter.addAction(PLAYER_MOVE_ACTION);

        return intentFilter;
    }

}
