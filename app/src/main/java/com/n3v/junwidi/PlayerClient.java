package com.n3v.junwidi;


import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.n3v.junwidi.Datas.DeviceInfo;
import com.n3v.junwidi.Listener.MyClientTaskListener;
import com.n3v.junwidi.Services.MyClientTask;

import java.io.File;


public class PlayerClient extends AppCompatActivity implements MyClientTaskListener {
    //모든 변수는 밀리미터 단위를 사용하도록 함
    int user = 0;//사용자 식별번호, 호스트 기기에만 미디어컨트롤러가 나오도록 하기 위함(user 변수의 값이 1인 경우에만 나오게 함)
    int H = 0;//결정된 레이아웃의 길이
    int W = 0;
    int aX, bX, cX = 0;//좌표 이동을 위한 각 기기의 X값
    int aY, bY, cY = 0;//좌표 이동을 위한 각 기기의 Y값
    boolean start = false;
    public int back = 0;
    public int stopTime = 0;
    String fileName = "";
    long fileSize = 0;
    VideoView vv;

    DeviceInfo myDeviceInfo = null;
    String hostAddress = "";

    AsyncTask nowTask = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_client);

        Intent intent = getIntent();
        myDeviceInfo = intent.getParcelableExtra("myDeviceInfo");
        hostAddress = intent.getStringExtra("host_addr");

        getSupportActionBar().hide();


        W = myDeviceInfo.getMm_videoview_width();
        H = myDeviceInfo.getMm_videoview_height();
        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        //W = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, W, dm);
        //H = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, H, dm);
//        W = (int) myDeviceInfo.mmToPx(myDeviceInfo.getMm_videoview_width());
//        H = (int) myDeviceInfo.mmToPx(myDeviceInfo.getMm_videoview_height());


        W = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, W, dm);
        H = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, H, dm);

        aX = myDeviceInfo.getSetXValue();
        aY = myDeviceInfo.getSetYValue();

        aX = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, aX, dm);

        vv = findViewById(R.id.videoViewClient);
        //filename = this.getExternalFilesDir(null) + "/TogetherTheater";
        fileName = myDeviceInfo.getVideoName();

        String filePath = this.getExternalFilesDir(null) + "/TogetherTheater/" + fileName;

        File video = new File(filePath);
        if (video.exists()) {
            fileSize = video.length();
        }

        vv.setVideoPath(filePath);
        vv.seekTo(1);

//        ViewGroup.LayoutParams params = vv.getLayoutParams();
//        params.height = H;
//        params.width = W;
//        vv.setLayoutParams(params);
        vv.getLayoutParams().width = W;
        vv.getLayoutParams().height = H;
        vv.setX(aX);
        vv.requestLayout();
    }

    public void playVideo() {
        //vv.seekTo(stopTime);
        vv.start();
    }

    public void pauseVideo() {
        vv.getCurrentPosition();
        vv.pause();
        stopTime = vv.getCurrentPosition();
        //vv.seekTo(stopTime);
    }

    public void seekTimeVideo(int time) {
        vv.seekTo(time);
    }

    @Override
    public void onResume() {
        super.onResume();
        VideoView vv = findViewById(R.id.videoViewClient);
        vv.resume();
        if (stopTime > 0) {
            vv.seekTo(stopTime);
        }
    }

    @Override
    public void onPause() {
        VideoView vv = findViewById(R.id.videoViewClient);
        if (vv.canPause()) {
            stopTime = vv.getCurrentPosition();
            pauseVideo();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        VideoView vv = findViewById(R.id.videoViewClient);
        vv.stopPlayback();
        super.onDestroy();
    }

    //뒤로가기 버튼 눌러서 액티비티 종료한 경우(다른 기기로 종료 신호 인텐트 전달 기능 추가 필요)
//    @Override
//    public void onBackPressed() {
//        if (back == 1) {
//            super.onBackPressed();
//            back = 0;
//        }
//    }

    //Host 액티비티로부터 액티비티 종료 인텐트를 전달받을 경우에 실행되어야 함
    public void quitByHost() {
        finishWithResult();
    }

    public void finishWithResult() {
        Intent data = new Intent();
        setResult(RESULT_OK, data);
        finish();
    }

    public AsyncTask callClientTask(String mode) {
        return new MyClientTask(this, mode, hostAddress, myDeviceInfo, this, fileName, this.fileSize).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    //MyClientTaskListener Overriding


    @Override
    public void onEndWait() {
    }

    @Override
    public void progressUpdate(int progress) {
    }

    @Override
    public void onHandshaked() {
    }

    @Override
    public void setFile(String fileName, long fileSize) {
    }

    @Override
    public void onReceiveFinished() {
    }

    @Override
    public void onReceiveCancelled() {
    }

    @Override
    public void onVideoAlreadyExist() {
    }

    @Override
    public void onReceiveShowGuideline() {
    }

    @Override
    public void onPreparePlayReceived() {
    }

    @Override
    public void onEndExcute() {
    }

    @Override
    public void onReceiveConPlay() {
        stopTime = 0;
        playVideo();
        nowTask = callClientTask(MyClientTask.CLIENT_CONTROL_WAITING_SERVICE);
    }

    @Override
    public void onReceiveConPause() {
        pauseVideo();
        nowTask = callClientTask(MyClientTask.CLIENT_CONTROL_WAITING_SERVICE);
    }

    @Override
    public void onReceiveConResume() {
        playVideo();
        nowTask = callClientTask(MyClientTask.CLIENT_CONTROL_WAITING_SERVICE);
    }

    @Override
    public void onReceiveConStop() {
        pauseVideo();
        finishWithResult();
    }

    @Override
    public void onReceiveConSeek(int time) {
        seekTimeVideo(time);
        nowTask = callClientTask(MyClientTask.CLIENT_CONTROL_WAITING_SERVICE);
    }
}
//온터치 , 온트랙볼 이벤트 ( 재생,일시정지,영상위치이동 ) - 미디어컨트롤러로 대체
//액티비티 종료할 시 모든 기기에서 액티비티 종료( 이전 액티비티로 이동)