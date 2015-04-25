package com.afandian.langstroth;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.util.Calendar;

/**
 * Created by joe on 24/04/2015.
 */
public class AlarmReceiver extends WakefulBroadcastReceiver {
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    private String baseDirectory;


    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, RecordingService.class);

        // This is called in a different context to setAlarm so this.baseDirectory isn't set.
        String baseDirectory = intent.getStringExtra("baseDirectory");
        service.putExtra("baseDirectory", baseDirectory);

        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, service);
    }

    public void setAlarm(Context context, String baseDirectory) {
        // Just in case.
        this.cancelAlarm(context);

        this.baseDirectory = baseDirectory;

        this.alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        // Order of these 3 lines is significant.
        // http://stackoverflow.com/questions/12470453/send-data-to-the-alarm-manager-broadcast-receiver
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("baseDirectory", baseDirectory);
        this.alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 03);
        long initialAlarm = calendar.getTimeInMillis();
        initialAlarm = 0;

        int MINUTE_REPEAT = 1;

        this.alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, initialAlarm,
                1000 * 60 * MINUTE_REPEAT, alarmIntent);
    }

    public void cancelAlarm(Context context) {
        if (this.alarmMgr!= null) {
            this.alarmMgr.cancel(alarmIntent);
        }
    }
}
