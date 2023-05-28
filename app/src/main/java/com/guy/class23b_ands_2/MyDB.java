package com.guy.class23b_ands_2;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public class MyDB {

    public static boolean isNeedToRun(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("MYDB", MODE_PRIVATE);
        boolean result = prefs.getBoolean("KEY_NEED_TO_RUN", false);
        return result;
    }

    public static void saveState(Context context, boolean state) {
        SharedPreferences prefs = context.getSharedPreferences("MYDB", MODE_PRIVATE);
        prefs.edit().putBoolean("KEY_NEED_TO_RUN", state).commit();
    }
}
