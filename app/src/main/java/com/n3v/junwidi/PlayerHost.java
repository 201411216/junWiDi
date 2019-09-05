package com.n3v.junwidi;


import android.content.res.Resources;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.n3v.junwidi.R;


public class PlayerHost extends AppCompatActivity {
    //받아오는 데이터 목록
    public WifiP2pDevice wifiP2pDevice;
    public String str_address;
    public int px_width;
    public int px_height;
    public int dpi;
    public float density;
    public boolean isGroupOwner;
    //
    DisplayMetrics metrics = new DisplayMetrics();
    int user = 1;//사용자 식별번호, 호스트 기기에만 미디어컨트롤러가 나오도록 하기 위함(user 변수의 값이 1인 경우에만 나오게 함)
    int aW, bW, cW, aH, bH, cH = 0;//화면 분할을 위한 각 디바이스 가로세로 길이
    int sH = 0;//기기 a b c 중 가장 작은 높이값
    int H = 1500;//결정된 레이아웃의 길이
    int W = 3000;
    int aX, bX, cX = 0;//좌표 이동을 위한 각 기기의 X값
    int aY, bY, cY = 0;
    public int stopTime = 0;
    public int back = 0;
    VideoView vv;
    Button btnStart, btnPause;
    //


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_host);
        //시작,일시정지 버튼
        btnStart = findViewById(R.id.btnStart);
        btnPause = findViewById(R.id.btnPause);
        //비디오뷰 생성
        vv = findViewById(R.id.videoView1);
        Uri video = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.test2);
        vv.setVideoURI(video);
        mediaController();
        //비디오뷰의 높이 설정을 위한 디바이스들 중 높이 최소값 구해 sH에 저장
        if (aH < bH) {
            if (aH < cH) {
                sH = aH;
            } else {
                sH = cH;
            }
        } else {
            if (cH < bH) {
                sH = cH;
            } else {
                sH = bH;
            }
        }
        //결정된 레이아웃의 높이, 넓이값 정의
        W = aW + bW + cW;
        H = W / 16 * 9;
        if (H > sH) {
            W = sH / 9 * 16;
        }
        //기기마다 다른 setX,Y값 지정을 위함
        aX = 0;
        bX = -aW;
        cX = -aW - bW;
        aY = aH - H;
        bY = bH - H;
        cY = cH - H;

        //비디오뷰 사이즈 조절
        ViewGroup.LayoutParams params = vv.getLayoutParams();
        //밀리미터 단위로 단위변환
        int ww=300;
        int hh=200;
        PxToMm(ww,metrics);
        PxToMm(hh,metrics);
        ww=(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM,ww,getResources().getDisplayMetrics());
        hh=(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM,hh,getResources().getDisplayMetrics());
        //비디오뷰 사이즈 결정
        params.height = ww;
        params.width = hh;
        vv.setLayoutParams(params);
        vv.setX(aX);
        vv.setY(aY);
    }

    public int DpToPx(int dp){
        Resources resources = this.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        int px = dp * (metrics.densityDpi / 160);
        return px;
    }

    //px을 dp로 변환 (px을 입력받아 dp를 리턴)
    public int PxToDp(int px){
        Resources resources = this.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        int dp = px / (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
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
    public int PxToMm(int value,DisplayMetrics metrics){
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
        if (back == 1) {
            super.onBackPressed();
            back = 0;
        }
    }

    //Client 액티비티로부터 액티비티 종료 인텐트를 전달받았을 때 실행되어야 함
    public void quitByClient() {
        finish();
    }
}
//pause가 걸리는 경우 - 전화, 다른 앱의 메세지, 팝업 등의 불가피한 pause ----나머지 기기들은 영상 일시정지
//                   - 뒤로가기 버튼으로 임의로 액티비티 종료 --------------나머지 기기들은 액티비티 종료