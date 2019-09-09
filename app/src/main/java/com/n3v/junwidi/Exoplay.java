package com.n3v.junwidi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.*;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.offline.FilteringManifestParser;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.manifest.DashManifestParser;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.n3v.junwidi.ServerActivity;
import com.n3v.junwidi.Utils.RealPathUtil;

import java.io.File;


public class Exoplay extends AppCompatActivity {

    private PlayerView exoPlayerView;
    private SimpleExoPlayer player;

    private Boolean playWhenReady = true;
    private int currentWindow = 0;
    private Long playbackPosition = 0L;
    private String myVideoPath ="";
    private Uri localUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exoplay);
        Intent intent = getIntent();
        myVideoPath=intent.getStringExtra("videoPath");
        String filePath=myVideoPath;
        File file=new File(filePath);
        localUri=Uri.fromFile(file);


        exoPlayerView = findViewById(R.id.exoPlayerView);
    }


    @Override
    protected void onStart() {
        super.onStart();
        initializePlayer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        releasePlayer();
    }



    private void initializePlayer() {
        if (player == null) {
            player = ExoPlayerFactory.newSimpleInstance(this.getApplicationContext());
            //플레이어 연결
            exoPlayerView.setPlayer(player);
            player.setPlayWhenReady(playWhenReady);
            player.seekTo(currentWindow, playbackPosition);
        }
        //Uri videoURI = data.getData();
        //videoPath = RealPathUtil.getRealPath(this, videoURI);

        //String sample = "https://www.apple.com/105/media/kr/airpods/2019/11e01da6_a4d6_4094_96b5_81c74cbf7d95/films/cases/airpods-cases-tpl-kr-2019_1280x720h.mp4";

        MediaSource mediaSource = buildMediaSource(localUri);



        //prepare
        player.prepare(mediaSource, true, false);

        //start,stop
        player.setPlayWhenReady(playWhenReady);
    }



    private MediaSource buildMediaSource(Uri uri) {

        return new ExtractorMediaSource(uri,

                new DefaultDataSourceFactory(this,"Together Theater"),

                new DefaultExtractorsFactory(), null, null);

    }


    private void releasePlayer() {
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();

            exoPlayerView.setPlayer(null);
            player.release();
            player = null;

        }
    }
}
