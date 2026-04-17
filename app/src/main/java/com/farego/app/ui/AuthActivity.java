package com.farego.app.ui;
// ============================================================
// FILE: app/src/main/java/com/farego/app/ui/AuthActivity.java
// PURPOSE: Single-activity host for LoginFragment and RegisterFragment.
//          Uses Jetpack Navigation for fragment switching.
// ============================================================

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.farego.app.R;

public class AuthActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        // NavHostFragment defined in activity_auth.xml handles routing
        // between LoginFragment and RegisterFragment via nav_auth_graph.xml
    }
}