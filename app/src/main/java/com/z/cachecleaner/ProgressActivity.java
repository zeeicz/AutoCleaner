package com.z.cachecleaner;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class ProgressActivity extends AppCompatActivity {
    
    private int currentIndex = 0;
    private List<AppInfo> selectedApps = new ArrayList<>();
    private WindowManager windowManager;
    private View overlayView;
    private boolean isCancelled = false; // Flag pembatalan

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        overlayView = LayoutInflater.from(this).inflate(R.layout.activity_progress, null);

        // Pengaturan agar FULL SCREEN menutupi status bar dan navigasi
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | 
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, // Tetap gunakan ini agar robot bisa ketik/klik di belakang
                PixelFormat.TRANSLUCENT
        );

        windowManager.addView(overlayView, params);

        // LOGIKA TOMBOL BATAL
        Button btnCancel = overlayView.findViewById(R.id.btnCancelOverlay);
        btnCancel.setOnClickListener(v -> {
            isCancelled = true;
            tutupOverlay();
        });

        for (AppInfo app : MainActivity.appList) {
            if (app.isSelectToClean) {
                selectedApps.add(app);
            }
        }

        if (selectedApps.isEmpty()) {
            tutupOverlay();
            return;
        }

        startCleaningLoop();
    }

    private void startCleaningLoop() {
        if (isCancelled || currentIndex >= selectedApps.size()) {
            tutupOverlay();
            return;
        }

        AppInfo currentApp = selectedApps.get(currentIndex);

        TextView tvIndex = overlayView.findViewById(R.id.tvProgressIndex);
        tvIndex.setText("Proses " + (currentIndex + 1) + "/" + selectedApps.size());
        
        ImageView ivIcon = overlayView.findViewById(R.id.ivCurrentAppIcon);
        ivIcon.setImageDrawable(currentApp.icon);
        
        TextView tvName = overlayView.findViewById(R.id.tvCurrentAppName);
        tvName.setText(currentApp.appName);

        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + currentApp.packageName));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);

        new Handler().postDelayed(() -> {
            if (!isCancelled) {
                currentIndex++;
                startCleaningLoop();
            }
        }, 2500); 
    }

    private void tutupOverlay() {
        isCancelled = true;
        if (overlayView != null && windowManager != null) {
            try {
                windowManager.removeView(overlayView);
            } catch (Exception e) { e.printStackTrace(); }
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (overlayView != null && overlayView.getWindowToken() != null) {
            windowManager.removeView(overlayView);
        }
    }
}