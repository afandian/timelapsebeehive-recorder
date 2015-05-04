package com.afandian.langstroth;

import android.app.IntentService;
import android.content.Intent;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * Created by joe on 24/04/2015.
 */
public class RecordingService extends IntentService {
    private LangstrothApplication application;
    public RecordingService() {
        super("SchedulingService");
    }

    public void onCreate() {
        super.onCreate();

        this.application = (LangstrothApplication)this.getApplication();
    }

    public void triggerRaw(int duration)  {
        Date now = new Date();

        String filename = this.application.getStorage().getPathForRecording(now, duration);

        AudioRecorder recorder = new AudioRecorder();
        recorder.startRecording(filename);

        // Record for n seconds.
        // TODO can do this sample-accurately.
        try {
            Thread.sleep(duration);
        } catch (InterruptedException ex) {

        }

        recorder.stop();
        this.application.getStorage().save(filename, now, this);

        MediaScannerConnection.scanFile(this,  new String[] { filename }, null,null);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        int duration = intent.getIntExtra("duration", 5000);

        this.triggerRaw(duration);

        AlarmReceiver.completeWakefulIntent(intent);
    }
}
