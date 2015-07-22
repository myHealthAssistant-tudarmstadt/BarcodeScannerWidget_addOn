package com.ess.tudarmstadt.de.mwidgetexample.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;
import java.util.Random;


/*
 * Receives the alarm from the MainActivity and sets up a notification. passes it on to the second
 * alarm receiver.
 */
public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        Intent intentNew = new Intent(context, com.ess.tudarmstadt.de.mwidgetexample.utils.AlarmReceiver2.class);
        int time = intent.getIntExtra("time", -1);
        int hour = 0;
        switch (time) {
            case 0: hour = 8;
                break;
            case 1: hour = 10;
                break;
            case 2: hour = 12;
                break;
            case 3: hour = 14;
                break;
            case 4: hour = 16;
                break;
            case 5: hour = 18;
                break;
        }
        intentNew.putExtra("time", time);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, time, intentNew, PendingIntent.FLAG_UPDATE_CURRENT);
        String usage = intent.getStringExtra("usage");
        Calendar cal = Calendar.getInstance();
        Random r = new Random();
        int minute = r.nextInt(60);
        cal.set(Calendar.MINUTE, minute);
        if (usage.equals("create")) {
            if(cal.get(Calendar.HOUR_OF_DAY) <= hour) {
                alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                        cal.getTimeInMillis(),
                        AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
            } else {
                alarmManager.cancel(pendingIntent);
            }
        } else if (usage.equals("delete")) {
            AlarmReceiver2.counter = 4;
            alarmManager.cancel(pendingIntent);
        }
    }
}