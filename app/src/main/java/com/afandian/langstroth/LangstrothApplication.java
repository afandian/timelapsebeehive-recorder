package com.afandian.langstroth;

import android.app.Application;

/**
 * Global application state. Set by Storage.
 */
public class LangstrothApplication extends Application {
    private boolean authenticated;
    private String langstrothCookie;
    private Storage storage;
    private String userId;

    public LangstrothApplication() {
        super();


    }

    public void onCreate() {
        this.storage = new Storage(this, this);
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public boolean isAuthenticated() {
        return this.authenticated;
    }

    public String getLangstrothCookie() {
        return this.langstrothCookie;
    }

    public void setLangstrothCookie(String cookie) {
        this.langstrothCookie = cookie;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Storage getStorage() {
        return this.storage;
    }
}
