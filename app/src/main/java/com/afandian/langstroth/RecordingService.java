package com.afandian.langstroth;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by joe on 24/04/2015.
 */
public class RecordingService extends IntentService {
    // A new MediaRecorder is acquired each time this fires.
    MediaRecorder recorder = null;

    String baseDirectory;

    public RecordingService() {
        super("SchedulingService");
    }

    public RecordingService(String baseDirectory) {
        super("SchedulingService");
        this.baseDirectory = baseDirectory;
    }

    class MediaListener implements MediaRecorder.OnInfoListener {
        public void onInfo(MediaRecorder mr, int what, int extra) {
            if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                RecordingService.this.disposeAudio();
            }
        }
    }

    private void disposeAudio() {
        this.recorder.stop();
        this.recorder.reset();
        this.recorder.release();
        this.recorder = null;
    }

    public void trigger() {
        if (this.recorder == null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.UK);
            String filename = baseDirectory + "/" + sdf.format(new Date()) + ".mp4";

            this.recorder = new MediaRecorder();
            this.recorder.setOnInfoListener(new MediaListener());

            recorder.reset();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            recorder.setOutputFile(filename);
            recorder.setMaxDuration(1000 * 5);

            try {
                recorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("Langstroth", e.getMessage());
            }

            recorder.start();   // Recording is now started
        } else {
            Log.e("Langstroth", "Busy!");
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        this.baseDirectory = intent.getStringExtra("baseDirectory");
        this.trigger();
    }
}
