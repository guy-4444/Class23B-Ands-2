package com.guy.class23b_ands_2;

import android.app.Application;
import android.location.Location;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        MCT5.initHelper();
    }
}
