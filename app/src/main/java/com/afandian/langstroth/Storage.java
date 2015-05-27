package com.afandian.langstroth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpProtocolParams;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLConnection;
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

    // This is always constructed by and belongs to a context.
    private Context context;

    public static final String PREFS_NAME = "LangstrothPrefs";
    public static final String LANGSTROTH_COOKIE_NAME = "LangstrothCookie";

    private final static String LOGIN_ENDPOINT = "http://192.168.1.99:6060/login";
    private final static String AUTHENTICATED_ENDPOINT = "http://192.168.1.99:6060/authenticated";
    private final static String RECORDINGS_ENDPOINT = "http://192.168.1.99:6060/recordings";

    // All temporary state (cookie, authenticated) is stored here for other instances of Storage to find.
    private LangstrothApplication application;

    private UploadTask uploadTask;

    public Storage(Context context, LangstrothApplication application) {
        this.application = application;

        this.veryBaseDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        this.baseDir = new File(this.veryBaseDir, "Langstroth");
        this.basePath = this.baseDir.getAbsolutePath();
        this.context = context;

        this.readPrefs();


        this.attemptAuthencate();
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

    public void deleteAll(File base) {
        File[] files = base.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".wav")) {
                    file.delete();
                } else if (file.isDirectory()) {
                    deleteAll(file);
                }
            }
        }

    }

    public void deleteAll() {
        deleteAll(baseDir);
    }


    public class AuthenticateTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {

        HttpClient httpClient = new DefaultHttpClient();
        HttpGet get = new HttpGet(AUTHENTICATED_ENDPOINT);

        get.setHeader(new BasicHeader("Cookie", Storage.this.application.getLangstrothCookie()));
        HttpProtocolParams.setUseExpectContinue(httpClient.getParams(), false);

        try {
            HttpResponse response = httpClient.execute(get);
            StatusLine statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                Storage.this.application.setAuthenticated(true);

                for (Header header : response.getHeaders("User-Id")) {
                    String userId = header.getValue();
                    Storage.this.application.setUserId(userId);
                    return true;
                }

                return true;
            } else{
                response.getEntity().getContent().close();
                Storage.this.setCookie(null);
                Storage.this.application.setAuthenticated(false);
                return false;
            }
        } catch (IOException e) {
            // TODO log
            Storage.this.setCookie(null);
            return false;
        }
    }
    }

    public class UploadTask extends AsyncTask<Void, Void, Boolean> {
        private File baseDir;

        UploadTask(File baseDir) {
            this.baseDir = baseDir;

        }

        private void uploadDir(File base, String cookie) {
            File[] files = base.listFiles();
            if (files == null) {
                return;
            } else {
                for (File file : files) {
                    if (file.isFile()) {
                        // URL is http://xxx/recordings/«user-id»/«duration»/
                        String duration = file.getParentFile().getName();
                        String filename = file.getName();

                        // Hardcoded to 1 beehive for now.
                        String entity = "1";
                        String url = RECORDINGS_ENDPOINT + "/" + Storage.this.application.getUserId() + "/" + entity + "/" + duration + "/" + filename;

                        Log.e("Langstroth", "UPlOAD" + file.getAbsolutePath() + " TO " + url);

                        HttpClient httpClient = new DefaultHttpClient();

                        HttpPut put = new HttpPut(url);
                        put.setHeader(new BasicHeader("Cookie", cookie));
                        HttpProtocolParams.setUseExpectContinue(httpClient.getParams(), false);

                        put.setEntity(new FileEntity(file, "binary/octet-stream"));

                        try {
                            HttpResponse response = httpClient.execute(put);

                            StatusLine statusLine = response.getStatusLine();
                            if(statusLine.getStatusCode() == HttpStatus.SC_OK || statusLine.getStatusCode() == HttpStatus.SC_CREATED){
                                Log.e("Langstroth", "UPlOADED " + url);
                                file.delete();
                            } else{
                                Log.e("Langstroth", "NOT UPLOADED " + statusLine.getStatusCode());
                                response.getEntity().getContent().close();

                            }
                        } catch (IOException e) {
                            // TODO log

                        }
                    } else if (file.isDirectory()) {
                        uploadDir(file, cookie);
                    }
                }
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String cookie = Storage.this.application.getLangstrothCookie();
            uploadDir(this.baseDir, cookie);
            return true;
        }
    }

    public void upload() {
        // TODO signal to main activity on failure.
        this.uploadTask = new UploadTask(baseDir);
        this.uploadTask.execute((Void) null);
    }

    public void save(String path, Date date, Context context) {
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
                    Log.e("Langstroth", path);
                } else if (file.isDirectory()) {
                    this.scan(context, file);
                }
            }
        }
    }

    public void scan(Context context) {
        this.scan(context, this.baseDir);
    }

    private void readPrefs() {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        this.application.setLangstrothCookie(settings.getString(LANGSTROTH_COOKIE_NAME, null));
    }


    public boolean isAuthenticated() {
        // May have been set by a different instance.
        this.readPrefs();

        if (this.application.getLangstrothCookie()== null) {
            return false;
        } else {
            return this.application.isAuthenticated();
        }
    }

    private void attemptAuthencate() {
        AuthenticateTask task = new AuthenticateTask();
        task.execute((Void) null);
    }

    // Attempt login, save cookie.
    public boolean tryLogin(String username, String password) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet get = new HttpGet(LOGIN_ENDPOINT);

        String encoding = Base64.encodeToString((username + ":" + password).getBytes(), Base64.NO_WRAP);
        get.setHeader(new BasicHeader("Authorization", "Basic " + encoding));
        HttpProtocolParams.setUseExpectContinue(httpClient.getParams(), false);

        try {
            HttpResponse response = httpClient.execute(get);
            StatusLine statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() == HttpStatus.SC_OK){

                // There's only one cookie and user id.
                for (Header header : response.getHeaders("Set-Cookie")) {
                    String cookie = header.getValue();
                    this.setCookie(cookie);
                    return true;
                }

                for (Header header : response.getHeaders("User-Id")) {
                    String userId = header.getValue();
                    this.application.setUserId(userId);
                    return true;
                }

            } else{
                response.getEntity().getContent().close();
                this.setCookie(null);
                return false;
            }
        } catch (IOException e) {
            // TODO log
            this.setCookie(null);
            return false;
        }

        return false;
    }


    public void setCookie(String cookie) {
        SharedPreferences settings = this.context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(LANGSTROTH_COOKIE_NAME, cookie);
        this.application.setLangstrothCookie(cookie);
        editor.commit();
    }
}
