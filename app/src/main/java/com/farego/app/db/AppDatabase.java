package com.farego.app.db;

// ============================================================
// FILE: app/src/main/java/com/farego/app/db/AppDatabase.java
// PURPOSE: Room database singleton. Seeds fare rates and default
//          admin account on first creation (offline fallback).
// ============================================================

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.farego.app.db.dao.FareRateDao;
import com.farego.app.db.dao.SearchHistoryDao;
import com.farego.app.db.dao.UserDao;
import com.farego.app.db.entity.FareRate;
import com.farego.app.db.entity.SearchHistory;
import com.farego.app.db.entity.User;
import com.farego.app.utils.HashUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
        entities  = { FareRate.class, User.class, SearchHistory.class },
        version   = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    // ── DAOs ──────────────────────────────────────────────────
    public abstract FareRateDao      fareRateDao();
    public abstract UserDao          userDao();
    public abstract SearchHistoryDao searchHistoryDao();

    // ── Singleton ─────────────────────────────────────────────
    private static volatile AppDatabase INSTANCE;

    /** Background executor for all DB writes off the main thread */
    public static final ExecutorService dbExecutor =
            Executors.newFixedThreadPool(4);

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "farego_database"
                            )
                            .addCallback(seedCallback)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // ── Seed callback — runs once on first DB creation ────────
    private static final RoomDatabase.Callback seedCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            dbExecutor.execute(() -> {
                // Get the instance directly inside the thread
                AppDatabase database = INSTANCE;
                if (database == null) return;

                FareRateDao fareRateDao = database.fareRateDao();
                UserDao     userDao     = database.userDao();

                // ── Seed default fare rates (offline fallback) ─
                long now = System.currentTimeMillis();
                fareRateDao.insertAll(java.util.Arrays.asList(
                        new FareRate("TroTro", 1.50, 0.80, 2.00, now),
                        new FareRate("Taxi",   3.00, 1.50, 5.00, now),
                        new FareRate("Uber",   4.00, 2.00, 7.00, now)
                ));

                // ── Seed default admin account ─────────────────
                // Username: admin  |  Password: admin123
                String adminHash = HashUtils.sha256("admin123");
                userDao.insertUser(new User(
                        "admin",
                        adminHash,
                        "admin@farego.app",
                        true,
                        now
                ));
            });
        }
    };
}