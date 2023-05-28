package com.guy.class23b_ands_2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;

public class Activity_Panel extends AppCompatActivity {


    private MaterialButton panel_BTN_start;
    private MaterialButton panel_BTN_stop;
    private MaterialTextView panel_LBL_info;

    BroadcastReceiver locationBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MyLoc myLoc = null;
            try {
                String json = intent.getStringExtra(LocationService.BROADCAST_NEW_LOCATION_EXTRA_KEY);
                myLoc = new Gson().fromJson(json, MyLoc.class);
                updateInfo(myLoc);
            } catch (Exception ex) {}
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel);

        panel_BTN_start = findViewById(R.id.panel_BTN_start);
        panel_BTN_stop = findViewById(R.id.panel_BTN_stop);
        panel_LBL_info = findViewById(R.id.panel_LBL_info);

        panel_BTN_start.setOnClickListener(v -> start());
        panel_BTN_stop.setOnClickListener(v -> stop());


        panel_BTN_stop.setVisibility(View.VISIBLE    );
        requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 104);

        MyReminder.startReminder(this);
    }

    private void updateInfo(MyLoc myLoc) {
        panel_LBL_info.setText(myLoc.getSpeed() + " km/h\n" + myLoc.getBearing());
    }


    private void start() {
        MyDB.saveState(this, true);
        sendActionToService(LocationService.START_FOREGROUND_SERVICE);
    }

    private void stop() {
        MyDB.saveState(this, false);
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


    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(LocationService.BROADCAST_NEW_LOCATION_100FM);
        LocalBroadcastManager.getInstance(this).registerReceiver(locationBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationBroadcastReceiver);
    }
}