package com.n3v.junwidi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void enterServer(View view) {
        startActivity(new Intent(this, ServerActivity.class));
    } //xml 파일을 통해 onClick으로 호출됨

    public void enterClient(View view) {
        startActivity(new Intent(this, ClientActivity.class));
    } //xml 파일을 통해 onClick으로 호출됨

}
