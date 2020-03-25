package com.example.firebaseapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void OpenTextRec(View v){
        Intent intent = new Intent(this, TextActivity.class);
        startActivity(intent);
    }
    public void OpenObjRec(View v){
        Intent intent = new Intent(this, DetectActivity.class);
        startActivity(intent);
    }
}
