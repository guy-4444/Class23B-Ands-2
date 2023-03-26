package com.guy.class23b_ands_2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.button.MaterialButton;

public class Activity_Panel extends AppCompatActivity {


    private MaterialButton panel_BTN_start;
    private MaterialButton panel_BTN_stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel);

        panel_BTN_start = findViewById(R.id.panel_BTN_start);
        panel_BTN_stop = findViewById(R.id.panel_BTN_stop);
        panel_BTN_start.setOnClickListener(v -> start());
        panel_BTN_stop.setOnClickListener(v -> stop());


    }

    private void start() {
        sendActionToService(LocationService.START_FOREGROUND_SERVICE);
    }
    private void stop() {
        sendActionToService(LocationService.STOP_FOREGROUND_SERVICE);
    }

    private void sendActionToService(String action) {
        Intent intent = new Intent(this, LocationService.class);
        intent.setAction(action);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
            // or
            //ContextCompat.startForegroundService(this, startIntent);
        } else {
            startService(intent);
        }
    }





}