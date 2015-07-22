package com.ess.tudarmstadt.de.mwidgetexample.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * Gets called when the system reboots to reset the alarmSetter variable.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs;
        prefs = context.getSharedPreferences("com.ess.tudarmstadt.utils.prefs", Context.MODE_PRIVATE);
        String setAlarmsString = "com.ess.tudarmstadt.utils.prefs.alarms";
        prefs.edit().putInt(setAlarmsString, 0).apply();
    }
}
