package com.project.weatherforecast;
/**
Author       : Devaraju Ratnala
Created Date : 03-02-2015 14:00
Purpose      : This is a Splash
APK Version  : 1.0
*/

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        moveToActivity();
    }

    private void moveToActivity() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadHomePage();
            }
        }, 1500);
    }

    private void loadHomePage() {

        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        finish();
    }
}
