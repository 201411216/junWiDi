package com.n3v.junwidi;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.n3v.junwidi.Datas.DeviceInfo;
import com.n3v.junwidi.Listener.MyServerTaskListener;
import com.n3v.junwidi.Services.MyServerTask;

import static java.lang.Thread.sleep;

public class PlayerHost extends AppCompatActivity implements MyServerTaskListener {

    //모든 변수는 밀리미터 단위를 사용하도록 함
    DisplayMetrics metrics = new DisplayMetrics();
    public int H;//결정된 레이아웃의 길이
    public int W;
    int aX;//좌표 이동을 위한 각 기기의 X값
    int aY;
    public int stopTime = 0;
    public int back = 0;
    VideoView vv = null;
    FrameLayout flh = null;
    Button btnStart, btnPause;
    SeekBar seekBar;
    boolean isPlaying = false;

    MyThread mt = null;

    DeviceInfo myDeviceInfo = null;

    AsyncTask nowTask = null;
    AsyncTask waitTask = null;


    class MyThread extends Thread {
        @Override
        public void run() {
            while (isPlaying) {
                seekBar.setProgress(vv.getCurrentPosition());
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
        setContentView(R.layout.player_host);
        //가이드라인 액티비티에서 비디오뷰 가로세로값,XY 좌표값 받아옴
        Intent intent = getIntent();
        myDeviceInfo = intent.getParcelableExtra("myDeviceInfo");
//        Bundle bundle = intent.getExtras();
//        W = bundle.getInt("videoWidth",0);
//        H = bundle.getInt("videoHeight",0);
//        aX = bundle.getInt("videoX",0);
//        aY = bundle.getInt("videoY",0);
        getSupportActionBar().hide();

        Log.v("PlayerHost", "longStr : " + myDeviceInfo.getLongString());

        Log.v("PlayerHost", "before WH");

        W = myDeviceInfo.getMm_videoview_width();
        H = myDeviceInfo.getMm_videoview_height();

        Log.v("PlayerHost", " W = " + W + " / H = " + H);

        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();

        //W = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, W, dm);
        //H = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, H, dm);

//        W = (int) myDeviceInfo.mmToPx(myDeviceInfo.getMm_videoview_width());
//        H = (int) myDeviceInfo.mmToPx(myDeviceInfo.getMm_videoview_height());

        W = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, W, dm);
        H = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, H, dm);

        Log.v("PlayerHost", "afterAD : W = " + W + " / H = " + H);

        aX = myDeviceInfo.getSetXValue();
        aY = myDeviceInfo.getSetYValue();

        aX = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, aX, dm);

        //시작,일시정지 버튼
        btnStart = findViewById(R.id.btnStart);
        btnPause = findViewById(R.id.btnPause);
        //
        flh = findViewById(R.id.fLh);

        //비디오뷰 생성
        vv = findViewById(R.id.videoViewHost);
        //vv.setOnPreparedListener(onPrepared);

        String filePath = myDeviceInfo.getVideoName();
        //filePath = this.getExternalFilesDir(null) + "/TogetherTheater/" + filePath;
        Log.v("PlayerHost", "path : " + filePath);

        vv.setVideoPath(filePath);
        //vv.setVideoURI(uri);
        vv.seekTo(0);

        //비디오뷰 사이즈 조절

        vv.getLayoutParams().width = W;
        vv.getLayoutParams().height = H;

        vv.setX(aX);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(W, H);
        lp.leftMargin = 0;
        lp.topMargin = 0;
        lp.rightMargin = 0;
        lp.bottomMargin = 0;
        vv.setLayoutParams(lp);
        flh.requestLayout();


        waitTask = callServerTask(MyServerTask.SERVER_CONTROL_WAITING_SERVICE);

        //시크 바 생성
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isPlaying = true;
                int moveTime = seekBar.getProgress();
                stopTime = moveTime;
                vv.seekTo(moveTime);
                if (mt != null) {
                    mt.interrupt();
                }
                mt = new MyThread();
                mt.start();
                new MyThread().start();
                nowTask = callServerTask(MyServerTask.SERVER_CONTROL_SEND_SEEKTIME_SERVICE);
                vv.start();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isPlaying = false;
                vv.pause();
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (seekBar.getMax() == progress) {
                    isPlaying = false;
                    vv.stopPlayback();
                }
            }
        });
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    btnStart.setEnabled(false);
                    if (vv.canPause()) {
                        pauseVideo();
                    }
                    stopTime = vv.getCurrentPosition();
                    vv.seekTo(stopTime);
                    nowTask = callServerTask(MyServerTask.SERVER_CONTROL_SEND_SEEKTIME_SERVICE);
                    if (mt != null) {
                        mt.interrupt();
                    }
                    isPlaying = true;
                    mt = new MyThread();
                    mt.start();
                    vv.start();
                    btnStart.setEnabled(true);
                } else {
                    int a = vv.getDuration();
                    seekBar.setMax(a);
                    new MyThread().start();
                    isPlaying = true;
                    nowTask = callServerTask(MyServerTask.SERVER_CONTROL_SEND_PLAY_SERVICE);
                    vv.start();
                }
            }
        });

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTime = vv.getCurrentPosition();
                vv.pause();
                isPlaying = false;
                nowTask = callServerTask(MyServerTask.SERVER_CONTROL_SEND_PAUSE_SERVICE);
            }
        });

        vv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {
                if (seekBar.getVisibility() == View.VISIBLE) {
                    btnStart.setVisibility(View.GONE);
                    btnPause.setVisibility(View.GONE);
                    seekBar.setVisibility(View.GONE);
                } else {
                    btnStart.setVisibility(View.VISIBLE);
                    btnPause.setVisibility(View.VISIBLE);
                    seekBar.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });

        vv.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setLooping(true);
            }
        });
    }

    public AsyncTask callServerTask(String mode) {
        return new MyServerTask(this, mode, myDeviceInfo.getStr_address(), myDeviceInfo, null, null, "", this, stopTime).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
        isPlaying = false;
    }

    //재생 - 재생 신호를 다른 기기로 전달하는 기능 추가 필요
    //Handler 이용해 현재 시간으로부터 2000밀리세컨드 후에 재생 Method 호출(vv.start())
    public void playVideo() {
        //vv.seekTo(stopTime);
        vv.start();
        isPlaying = true;
    }

    public void seekTimeVideo(int time) {
        vv.seekTo(time);
    }

//    private MediaPlayer.OnVideoSizeChangedListener onVideoSizeChangedListener =
//            new MediaPlayer.OnVideoSizeChangedListener() {
//                public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
//                    ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(W, H);
//                    vv.setLayoutParams(lp);
//                }
//            };
//
//    private MediaPlayer.OnPreparedListener onPrepared = new MediaPlayer.OnPreparedListener() {
//        public void onPrepared(MediaPlayer mp) {
//            mp.setOnVideoSizeChangedListener(onVideoSizeChangedListener);
//
//            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(W, H);
//            vv.setLayoutParams(lp);
//        }
//    };

    @Override
    public void onResume() {
        vv.resume();
        if (stopTime > -1) {
            vv.seekTo(stopTime);
        }

        //vv.resume();

        super.onResume();
    }

    @Override
    public void onPause() {
        if (vv.canPause()) {
            stopTime = vv.getCurrentPosition();
            pauseVideo();
        }
        super.onPause();
        isPlaying = false;
        btnStart.setVisibility(View.VISIBLE);
        btnPause.setVisibility(View.VISIBLE);
        nowTask = callServerTask(MyServerTask.SERVER_CONTROL_SEND_PAUSE_SERVICE);
    }

    @Override
    public void onDestroy() {
        VideoView vv = findViewById(R.id.videoViewHost);
        vv.stopPlayback();
        nowTask = callServerTask(MyServerTask.SERVER_CONTROL_SEND_STOP_SERVICE);
        super.onDestroy();
    }

    //뒤로가기 버튼 눌러서 액티비티 종료한 경우(다른 기기로 종료 신호 인텐트 전달 기능 추가 필요)
    @Override
    public void onBackPressed() {
        finishWithResult();
        super.onBackPressed();
    }

    //Client 액티비티로부터 액티비티 종료 인텐트를 전달받았을 때 실행되어야 함
    public void quitByClient() {
        finish();
    }

    public void finishWithResult() {
        Log.v("playerHost", "FWR");
        Intent data = new Intent();
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void progressUpdate(int progress) {
    }

    @Override
    public void onSendFinished() {
    }

    @Override
    public void onHandshaked() {
    }

    @Override
    public void onAllSendFinished() {
    }

    @Override
    public void onWaiting() {
    }

    @Override
    public void onCancelTransfer() {
    }

    @Override
    public void onNotify() {
    }

    @Override
    public void onShowGuidelineSended() {
    }

    @Override
    public void onPreparePlay() {
    }

    @Override
    public void onStartPlayer() {
    }

    @Override
    public void onReceiveConPause() {
        pauseVideo();
        waitTask = callServerTask(MyServerTask.SERVER_CONTROL_WAITING_SERVICE);
    }

    @Override
    public void onReceiveConPlay() {
        waitTask = callServerTask(MyServerTask.SERVER_CONTROL_WAITING_SERVICE);
    }

    @Override
    public void onReceiveConStop() {
        finishWithResult();
    }

    @Override
    public void onReceiveConSeek() {
        waitTask = callServerTask(MyServerTask.SERVER_CONTROL_WAITING_SERVICE);
    }
}
//pause가 걸리는 경우 - 전화, 다른 앱의 메세지, 팝업 등의 불가피한 pause ----나머지 기기들은 영상 일시정지
//                   - 뒤로가기 버튼으로 임의로 액티비티 종료 --------------나머지 기기들은 액티비티 종료