package com.farego.app.repository;
// ============================================================
// FILE: app/src/main/java/com/farego/app/repository/AuthRepository.java
// PURPOSE: Handles user login, registration, and session validation.
//          All Room operations run on AppDatabase.dbExecutor.
// ============================================================

import android.content.Context;

import com.farego.app.db.AppDatabase;
import com.farego.app.db.dao.UserDao;
import com.farego.app.db.entity.User;
import com.farego.app.utils.HashUtils;

public class AuthRepository {

    private final UserDao userDao;

    public AuthRepository(Context context) {
        this.userDao = AppDatabase.getInstance(context).userDao();
    }

    public interface AuthCallback {
        void onSuccess(User user);
        void onError(String message);
    }

    // ── Login ─────────────────────────────────────────────────
    public void login(String username, String password, AuthCallback callback) {
        AppDatabase.dbExecutor.execute(() -> {
            String hash = HashUtils.sha256(password);
            User user = userDao.findByCredentials(username, hash);
            if (user != null) {
                callback.onSuccess(user);
            } else {
                callback.onError("Invalid username or password");
            }
        });
    }

    // ── Register ──────────────────────────────────────────────
    public void register(String username, String password,
                         String email, AuthCallback callback) {
        AppDatabase.dbExecutor.execute(() -> {
            // Check if username already taken
            User existing = userDao.findByUsername(username);
            if (existing != null) {
                callback.onError("Username already exists");
                return;
            }
            String hash = HashUtils.sha256(password);
            User newUser = new User(username, hash, email, false,
                    System.currentTimeMillis());
            long id = userDao.insertUser(newUser);
            if (id > 0) {
                newUser.id = (int) id;
                callback.onSuccess(newUser);
            } else {
                callback.onError("Registration failed — please try again");
            }
        });
    }

    // ── Session null-safety check (called at SplashActivity) ──
    public void validateSession(int storedUserId, AuthCallback callback) {
        AppDatabase.dbExecutor.execute(() -> {
            User user = userDao.findById(storedUserId);
            if (user != null) {
                callback.onSuccess(user);
            } else {
                // Stored userId no longer exists in Room → treat as logged out
                callback.onError("Session expired");
            }
        });
    }
}