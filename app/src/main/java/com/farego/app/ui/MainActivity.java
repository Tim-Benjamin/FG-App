package com.farego.app.ui;
// ============================================================
// FILE: app/src/main/java/com/farego/app/ui/MainActivity.java
// PURPOSE: Host Activity for HomeFragment, UserDashboard, Admin.
//          NavHostFragment handles all navigation.
//          Also manages MapLibre lifecycle forwarding.
// ============================================================

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.farego.app.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // NavHostFragment in activity_main.xml handles fragment routing
    }

    public NavController getNavController() {
        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        return navHostFragment != null ? navHostFragment.getNavController() : null;
    }
}