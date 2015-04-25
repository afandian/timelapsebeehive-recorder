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
        service.putExtra("baseDirectory", this.baseDirectory);

        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, service);
    }

    public void setAlarm(Context context, String baseDirectory) {
        this.baseDirectory = baseDirectory;

        this.alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        //intent.putExtra("baseDirectory", baseDirectory);

        this.alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 21);
        calendar.set(Calendar.MINUTE, 46);

        this.alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                1000 * 60 * 15, alarmIntent);
    }

    public void cancelAlarm(Context context) {
        if (this.alarmMgr!= null) {
            this.alarmMgr.cancel(alarmIntent);
        }
    }
}
