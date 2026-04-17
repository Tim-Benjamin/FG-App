package com.farego.app;
// ============================================================
// FILE: app/src/main/java/com/farego/app/FareGoApplication.java
// PURPOSE: Application class — initialises AppDatabase singleton
//          on first launch so Room is ready before any Activity.
// ============================================================

import android.app.Application;
import com.farego.app.db.AppDatabase;

public class FareGoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Warm up Room singleton (triggers seed callback on first run)
        AppDatabase.getInstance(this);
    }
}