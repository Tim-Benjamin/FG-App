package com.farego.app.ui;
// ============================================================
// FILE: app/src/main/java/com/farego/app/ui/SplashActivity.java
// PURPOSE: Entry point. Checks SharedPreferences session,
//          validates userId against Room, routes to Home or Auth.
// ============================================================

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.farego.app.R;
import com.farego.app.utils.SessionManager;
import com.farego.app.viewmodel.AuthViewModel;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY_MS = 1800;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SessionManager session = new SessionManager(this);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (session.isLoggedIn()) {
                // Null-safety guard: verify userId still exists in Room
                AuthViewModel authVm = new ViewModelProvider(this).get(AuthViewModel.class);
                authVm.loggedInUser.observe(this, user -> {
                    if (user != null) {
                        goToMain();
                    }
                });
                authVm.errorMsg.observe(this, err -> {
                    if (err != null) {
                        // userId invalid — clear stale session and go to auth
                        session.clearSession();
                        goToAuth();
                    }
                });
                authVm.validateSession(session.getUserId());
            } else {
                goToAuth();
            }
        }, SPLASH_DELAY_MS);
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void goToAuth() {
        startActivity(new Intent(this, AuthActivity.class));
        finish();
    }
}