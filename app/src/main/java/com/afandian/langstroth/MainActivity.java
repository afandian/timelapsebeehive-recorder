package com.afandian.langstroth;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;


public class MainActivity extends ActionBarActivity {
    private AlarmReceiver alarm = new AlarmReceiver();
    // Keep an instance for 'record now'.
//    private RecordingService recordingService;

    // If this is null then we're not in a schedule session.
    private Integer filesAtStartOfRecording = null;

    private String baseDirectory;

    TextView numFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File filesDir = this.getExternalFilesDir(Environment.DIRECTORY_MUSIC);

        this.baseDirectory = filesDir.getAbsolutePath();
//        this.recordingService = new RecordingService();

        boolean result;
        try {
            result = new File(filesDir, "INDEX").createNewFile();
        } catch (IOException e) {
            Log.e("Langstroth", "Can't create index file");
        }

        this.numFiles =  (TextView)findViewById(R.id.numFiles);

        this.refreshFileCount();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void start(View view) {
        this.filesAtStartOfRecording = new Integer(this.fileCount());
        alarm.setAlarm(this, this.baseDirectory);
    }

    public void stop(View view) {
        alarm.cancelAlarm(this);
        this.filesAtStartOfRecording = null;
        this.refreshFileCount();
    }

    public void recordNow(View view) {
        // TODO alarm.runOnce
//        recordingService.trigger();
    }

    private int fileCount() {
        return new File(this.baseDirectory).listFiles().length;
    }

    public void refreshFileCount() {
        int fileCount = this.fileCount();
        String message = Integer.toString(fileCount) + " files";

        if (this.filesAtStartOfRecording != null) {
            Integer newFiles = fileCount - this.filesAtStartOfRecording;
            message = message + ", " + newFiles.toString() + " since start of schedule";
        }
        numFiles.setText(message);
    }

    public void refreshFileCount(View view) {
        this.refreshFileCount();
    }
}
