package com.afandian.langstroth;

import android.app.IntentService;
import android.content.Intent;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * Created by joe on 24/04/2015.
 */
public class RecordingService extends IntentService {

    public RecordingService() {
        super("SchedulingService");
    }

    class MediaListener implements MediaRecorder.OnInfoListener {

        public MediaListener() {
        }

        public void onInfo(MediaRecorder rec, int what, int extra) {
            if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                rec.stop();
                rec.reset();
                rec.release();

                // We may have a wake lock from the Alarm.
                // Release it if we have it.
//                if (this.intent != null) {
//                    AlarmReceiver.completeWakefulIntent(this.intent);
//                }
            }
        }
    }

    public void triggerRaw(String baseDirectory)  {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.UK);
        String filename = baseDirectory + "/" + sdf.format(new Date()) + ".wav";

        AudioRecorder recorder = new AudioRecorder();
        recorder.startRecording(filename);

        // Record for 5 seconds.
        // TODO can do this sample-accurately.
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {

        }

        recorder.stop();

    }


    @Override
    protected void onHandleIntent(Intent intent) {
        String baseDirectory = intent.getStringExtra("baseDirectory");

        if (baseDirectory == null) {
            Log.e("Langstroth", "Tried to trigger alarm with no base directory");
        } else {
            this.triggerRaw(baseDirectory);
        }

        AlarmReceiver.completeWakefulIntent(intent);
    }


}
