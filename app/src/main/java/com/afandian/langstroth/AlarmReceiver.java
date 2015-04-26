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

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, RecordingService.class);

        // This is called in a different context to setAlarm so this.baseDirectory isn't set.
        String baseDirectory = intent.getStringExtra("baseDirectory");
        service.putExtra("baseDirectory", baseDirectory);

        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, service);

        // TODO notify MainActivity to update view.
    }

    public void setAlarm(Context context, String baseDirectory, MainActivity.duration duration, MainActivity.interval alarmInterval) {
        // Just in case.
        this.cancelAlarm(context);

        this.alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        // Order of these 3 lines is significant.
        // http://stackoverflow.com/questions/12470453/send-data-to-the-alarm-manager-broadcast-receiver
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("baseDirectory", baseDirectory);

        long durationMillis = 0;
        if (duration == MainActivity.duration.ONE_SECOND) {
            intent.putExtra("duration", 1000);
        } else if (duration == MainActivity.duration.FIVE_SECONDS) {
            intent.putExtra("duration", 5000);
        } else if (duration == MainActivity.duration.TEN_SECONDS) {
            intent.putExtra("duration", 10000);
        }

        this.alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        int minutes = 0;
        switch (alarmInterval) {
            case HOUR:
                minutes = 60;
                break;
            case THIRTY_MINUTES: minutes = 30 ;
                break;
            case FIFTEEN_MINUTES: minutes = 15 ;
                break;
            case FIVE_MINUTES: minutes = 5 ;
                break;
        }

        this.alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0, 1000 * 60 * minutes, alarmIntent);
    }



    public void cancelAlarm(Context context) {
        if (this.alarmMgr!= null) {
            this.alarmMgr.cancel(alarmIntent);
        }
    }
}
