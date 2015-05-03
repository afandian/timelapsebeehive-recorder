package com.afandian.langstroth;

import android.content.Context;
import android.content.Intent;
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
 * Created by joe on 26/04/2015.
 */
public class Storage {
    // The 'langstroth' base directory.
    private File baseDir;

    // The top level 'documents' directory.
    public File veryBaseDir;

    private String basePath;


    public Storage() {
        this.veryBaseDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        this.baseDir = new File(this.veryBaseDir, "Langstroth");
        this.basePath = this.baseDir.getAbsolutePath();
    }

    private int fileCount(File base) {
        int count = 0;
        File[] files = base.listFiles();
        if (files == null) {
            return 0;
        } else {
            for (File file : files) {
                if (file.isFile()) {
                    count ++;
                } else if (file.isDirectory()) {
                    count = count + fileCount(file);
                }
            }
        }

        return count;
    }

    public int fileCount() {
        // Expect a /samples/«duration»/files* so this is recursive.
        return this.fileCount(this.baseDir);
    }

    public void clear() {
        File[] files = baseDir.listFiles();
        for(File file : files) {
            file.delete();
        }
    }

    public void upload() {
        File[] files = baseDir.listFiles();
        for(File file : files) {
            // TODO
        }
    }

    public void save(String path, Date date, Context context) {
        // TODO - maybe save in a database

        Intent mediaScannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri fileContentUri = Uri.fromFile(new File(path));
        mediaScannerIntent.setData(fileContentUri);
        context.sendBroadcast(mediaScannerIntent);
    }

    public String getPathForRecording(Date now, int duration) {
        String directoryPath = this.basePath + "/sample/" + duration;
        // TODO do something with result...
        boolean success = new File(directoryPath).mkdirs();

        return directoryPath + "/" + now.getTime() + ".wav";
    }

    public void scan(Context context, File base) {
        File[] files = base.listFiles();
        if (files == null) {
            return;
        } else {
            for (File file : files) {
                if (file.isFile()) {
                    String path = file.getAbsolutePath();
                    MediaScannerConnection.scanFile(context, new String[]{path}, null, null);
                    Log.e("MyApp", path);
                } else if (file.isDirectory()) {
                    this.scan(context, file);
                }
            }
        }
    }

    public void scan(Context context) {
        this.scan(context, this.baseDir);
    }
}
