package com.n3v.junwidi;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;


public class PlayerHost extends AppCompatActivity {
    //받아오는 데이터 목록
    public boolean isGroupOwner;
    //모든 변수는 밀리미터 단위를 사용하도록 함
    DisplayMetrics metrics = new DisplayMetrics();
    int aW, bW, cW, aH, bH, cH = 0;//화면 분할을 위한 각 디바이스 가로세로 길이
    int sH = 0;//기기 a b c 중 가장 작은 높이값
    public int H;//결정된 레이아웃의 길이
    public int W;
    int aX, bX, cX;//좌표 이동을 위한 각 기기의 X값
    int aY, bY, cY;
    public int stopTime = 0;
    public int back = 0;
    VideoView vv;
    Button btnStart, btnPause;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_host);
        //가이드라인 액티비티에서 비디오뷰 가로세로값,XY 좌표값 받아옴
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        W = bundle.getInt("videoWidth",0);
        H = bundle.getInt("videoHeight",0);
        aX = bundle.getInt("videoX",0);
        aY = bundle.getInt("videoY",0);

        //시작,일시정지 버튼
        btnStart = findViewById(R.id.btnStart);
        btnPause = findViewById(R.id.btnPause);

        //비디오뷰 생성
        vv = findViewById(R.id.videoView1);
        Uri video = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.test2);
        vv.setVideoURI(video);
        mediaController();
        vv.seekTo(1);

        //비디오뷰 사이즈 조절
        ViewGroup.LayoutParams params = vv.getLayoutParams();
        params.width = W;
        params.height = H;
        vv.setLayoutParams(params);
        vv.setX(aX);

    }


    //user 변수의 값이 1일 경우(=호스트 기기일 경우) 미디어 컨트롤러 생성
    public void mediaController() {
        if (isGroupOwner = true) {
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(vv);
            mediaController.setPadding(0, 0, 0, 0);
            vv.setMediaController(mediaController);

        }
    }

    public int PxToMm(int value, DisplayMetrics metrics) {
        return value * metrics.densityDpi;
    }

    public void StartButton(View v) {
        playVideo();
    }

    public void PauseButton(View v) {
        pauseVideo();
    }

    //영상 재생,정지 동기화
    //일시정지 - 일시정지 신호를 다른 기기로 전달하는 기능 추가 필요
    public void pauseVideo() {
        vv.getCurrentPosition();
        vv.pause();
        stopTime = vv.getCurrentPosition();
        //vv.seekTo(stopTime);
    }

    //재생 - 재생 신호를 다른 기기로 전달하는 기능 추가 필요
    //Handler 이용해 현재 시간으로부터 2000밀리세컨드 후에 재생 Method 호출(vv.start())
    public void playVideo() {
        //vv.seekTo(stopTime);
        vv.start();
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
        super.onBackPressed();
    }

    //Client 액티비티로부터 액티비티 종료 인텐트를 전달받았을 때 실행되어야 함
    public void quitByClient() {
        finish();
    }
}
//pause가 걸리는 경우 - 전화, 다른 앱의 메세지, 팝업 등의 불가피한 pause ----나머지 기기들은 영상 일시정지
//                   - 뒤로가기 버튼으로 임의로 액티비티 종료 --------------나머지 기기들은 액티비티 종료