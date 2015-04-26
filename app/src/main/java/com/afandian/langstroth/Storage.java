package com.afandian.langstroth;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by joe on 26/04/2015.
 */
public class Storage {
    private File baseDir;
    private Context context;
    private String basePath;

    public Storage(Context context) {
        this.context = context;
        this.baseDir = this.context.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        this.basePath = this.baseDir.getAbsolutePath();
    }

    public int fileCount() {
        return baseDir.listFiles().length;
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



        }
    }
}
