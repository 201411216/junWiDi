package com.n3v.junwidi;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        getSupportActionBar().hide();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return true;
    }

    @Override
    public void onResume(){

        try {
            sleep(500);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }

        super.onResume();
    }


    public void enterServer(View view) {
        startActivity(new Intent(this, ServerActivity.class));
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
    } //xml 파일을 통해 onClick으로 호출됨

    public void enterClient(View view) {
        startActivity(new Intent(this, ClientActivity.class));
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
    } //xml 파일을 통해 onClick으로 호출됨

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }
}
