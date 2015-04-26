package com.afandian.langstroth;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import java.io.File;


public class MainActivity extends ActionBarActivity {
    private AlarmReceiver alarm = new AlarmReceiver();
    private Button startButton;
    private Button stopButton;

    public enum interval {HOUR, THIRTY_MINUTES, FIFTEEN_MINUTES, FIVE_MINUTES};
    public enum duration {ONE_SECOND, FIVE_SECONDS, TEN_SECONDS };

    private interval recordInterval = interval.FIFTEEN_MINUTES;
    private duration recordDuration = duration.FIVE_SECONDS;
    private boolean scheduleRunning = false;

    private RadioButton hour;
    private RadioButton thirtyMinutes;
    private RadioButton fifteenMinutes;
    private RadioButton fiveMinutes;

    private RadioButton tenSeconds;
    private RadioButton fiveSeconds;
    private RadioButton oneSecond;

    // If this is null then we're not in a schedule session.
    private Integer filesAtStartOfRecording = null;

    private String baseDirectory;

    TextView numFiles;

    private Storage storage = new Storage(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.numFiles =  (TextView)findViewById(R.id.numFiles);

        this.hour = (RadioButton)findViewById(R.id.hour);
        this.thirtyMinutes = (RadioButton)findViewById(R.id.thirtyMinutes);
        this.fifteenMinutes = (RadioButton)findViewById(R.id.fifteenMinutes);
        this.fiveMinutes = (RadioButton)findViewById(R.id.fiveMinutes);

        this.tenSeconds = (RadioButton)findViewById(R.id.tenSeconds);
        this.fiveSeconds = (RadioButton)findViewById(R.id.fiveSeconds);
        this.oneSecond = (RadioButton)findViewById(R.id.oneSecond);

        this.startButton = (Button)findViewById(R.id.start);
        this.stopButton = (Button)findViewById(R.id.stop);

        // Load state and update view.
        this.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void start(View view) {
        this.filesAtStartOfRecording = new Integer(this.storage.fileCount());
        alarm.setAlarm(this, this.baseDirectory, this.recordDuration, this.recordInterval);
        this.scheduleRunning = true;
        this.updateViewState();
    }

    public void stop(View view) {
        alarm.cancelAlarm(this);
        this.filesAtStartOfRecording = null;
        this.scheduleRunning = false;
        this.updateViewState();
    }


    public void updateViewState() {
        int fileCount = this.storage.fileCount();
        String message = Integer.toString(fileCount) + " files";

        if (this.filesAtStartOfRecording != null) {
            Integer newFiles = fileCount - this.filesAtStartOfRecording;
            message = message + ", " + newFiles.toString() + " since start of schedule";
        }
        numFiles.setText(message);

        if (this.scheduleRunning) {
            this.startButton.setEnabled(false);
            this.stopButton.setEnabled(true);

            this.hour.setEnabled(false);
            this.thirtyMinutes.setEnabled(false);
            this.fifteenMinutes.setEnabled(false);
            this.fiveMinutes.setEnabled(false);

            this.tenSeconds.setEnabled(false);
            this.fiveSeconds.setEnabled(false);
            this.oneSecond.setEnabled(false);
        } else {
            this.startButton.setEnabled(true);
            this.stopButton.setEnabled(false);

            this.hour.setEnabled(true);
            this.thirtyMinutes.setEnabled(true);
            this.fifteenMinutes.setEnabled(true);
            this.fiveMinutes.setEnabled(true);

            this.tenSeconds.setEnabled(true);
            this.fiveSeconds.setEnabled(true);
            this.oneSecond.setEnabled(true);
        }

        switch (this.recordInterval) {
            case HOUR: this.hour.setChecked(true); break;
            case THIRTY_MINUTES: this.thirtyMinutes.setChecked(true); break;
            case FIFTEEN_MINUTES: this.fifteenMinutes.setChecked(true); break;
            case FIVE_MINUTES: this.fiveMinutes.setChecked(true); break;
        }

        switch (this.recordDuration) {
            case TEN_SECONDS: this.tenSeconds.setChecked(true); break;
            case FIVE_SECONDS: this.fiveSeconds.setChecked(true); break;
            case ONE_SECOND: this.oneSecond.setChecked(true); break;
        }
    }

    public void refreshFileCount(View view) {
        this.updateViewState();
    }

    // Set the interval at which recording happens.
    private void setInterval(interval interv) {
        this.recordInterval = interv;
    }

    private void setDuration(duration dur) {
        this.recordDuration = dur;
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        if (checked) {
            switch (view.getId()) {

                // Interval
                case R.id.hour: this.setInterval(interval.HOUR); break;
                case R.id.thirtyMinutes: this.setInterval(interval.THIRTY_MINUTES); break;
                case R.id.fifteenMinutes: this.setInterval(interval.FIFTEEN_MINUTES); break;
                case R.id.fiveMinutes: this.setInterval(interval.FIVE_MINUTES); break;

                // Duration
                case R.id.tenSeconds: this.setDuration(duration.TEN_SECONDS); break;
                case R.id.fiveSeconds: this.setDuration(duration.FIVE_SECONDS); break;
                case R.id.oneSecond: this.setDuration(duration.ONE_SECOND); break;
            }
        }
    }

    public void erase(View view) {

        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Erase all files?")
                .setMessage("Are you sure you want to delete all files?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.this.storage.clear();
                        MainActivity.this.updateViewState();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null){
            super.onRestoreInstanceState(savedInstanceState);

            this.recordInterval = (interval) savedInstanceState.getSerializable("period");
            this.recordDuration = (duration) savedInstanceState.getSerializable("duration");
            this.scheduleRunning = savedInstanceState.getBoolean("scheduleRunning");
        }

        this.updateViewState();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putSerializable("period", this.recordInterval);
        savedInstanceState.putSerializable("duration", this.recordDuration);
        savedInstanceState.putBoolean("scheduleRunning", this.scheduleRunning);
    }
}
