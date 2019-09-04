package com.n3v.junwidi.Player;


import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.n3v.junwidi.R;


public class Client extends AppCompatActivity {

    int user = 2;//사용자 식별번호, 호스트 기기에만 미디어컨트롤러가 나오도록 하기 위함(user 변수의 값이 1인 경우에만 나오게 함)
    int H = 0;//결정된 레이아웃의 길이
    int W = 0;
    int aW, bW, cW, aH, bH, cH = 0;
    int aX, bX, cX = 0;//좌표 이동을 위한 각 기기의 X값
    int aY, bY, cY = 0;//좌표 이동을 위한 각 기기의 Y값
    public int back = 0;
    public int stopTime = 0;
    String filename;
    VideoView vv;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_client);
        vv = findViewById(R.id.videoView1);
        //filename = this.getExternalFilesDir(null) + "/TogetherTheater";
        Uri video = Uri.parse("android.resource://" + getPackageName() + "/"+ R.raw.test2);
        vv.setVideoURI(video);
        ViewGroup.LayoutParams params = vv.getLayoutParams();
        params.height = H;
        params.width = W;
        vv.setLayoutParams(params);
        //좌표는 픽셀단위임
        vv.setX(bX);
        vv.setY(bY);
    }

    public void pauseVideo() {
        vv.getCurrentPosition();
        vv.pause();
        stopTime = vv.getCurrentPosition();
        //vv.seekTo(stopTime);
    }
    @Override
    public void onResume() {
        super.onResume();
        VideoView vv = findViewById(R.id.videoView1);
        vv.resume();
        if (stopTime > 0) {
            vv.seekTo(stopTime);
        }
    }

    @Override
    public void onPause() {
        VideoView vv = findViewById(R.id.videoView1);
        if (vv.canPause()) {
            stopTime = vv.getCurrentPosition();
            pauseVideo();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        VideoView vv = findViewById(R.id.videoView1);
        vv.stopPlayback();
        super.onDestroy();
    }
    //뒤로가기 버튼 눌러서 액티비티 종료한 경우(다른 기기로 종료 신호 인텐트 전달 기능 추가 필요)
    @Override
    public void onBackPressed() {
        if ( back == 1){
            super.onBackPressed();
            back = 0;
        }
    }
    //Host 액티비티로부터 액티비티 종료 인텐트를 전달받을 경우에 실행되어야 함
    public void quitByHost(){
        finish();
    }
}
//온터치 , 온트랙볼 이벤트 ( 재생,일시정지,영상위치이동 ) - 미디어컨트롤러로 대체
//액티비티 종료할 시 모든 기기에서 액티비티 종료( 이전 액티비티로 이동)