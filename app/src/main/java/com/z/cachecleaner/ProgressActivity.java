package com.z.cachecleaner;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
    private boolean isCancelled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // MENGGUNAKAN OVERLAY AGAR LAYAR PUTIH KOKOH DI DEPAN
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        overlayView = LayoutInflater.from(this).inflate(R.layout.activity_progress, null);

        // KUNCI RAHASIA: 
        // FLAG_NOT_FOCUSABLE = Robot Accessibility tetap bisa membaca aplikasi di belakangnya
        // (Kita sengaja TIDAK memakai FLAG_NOT_TOUCHABLE agar tombol Batal bisa disentuh jarimu!)
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, 
                PixelFormat.TRANSLUCENT
        );

        windowManager.addView(overlayView, params);

        // Aktifkan Tombol Batal
        Button btnCancel = overlayView.findViewById(R.id.btnCancelOverlay);
        btnCancel.setOnClickListener(v -> {
            isCancelled = true;
            tutupOverlay();
        });

        // Kumpulkan data aplikasi yang dicentang
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

        // Update Layar Putih
        TextView tvIndex = overlayView.findViewById(R.id.tvProgressIndex);
        tvIndex.setText("Proses " + (currentIndex + 1) + "/" + selectedApps.size());
        
        ImageView ivIcon = overlayView.findViewById(R.id.ivCurrentAppIcon);
        if (currentApp.icon != null) {
            ivIcon.setImageDrawable(currentApp.icon);
        }
        
        TextView tvName = overlayView.findViewById(R.id.tvCurrentAppName);
        tvName.setText(currentApp.appName);

        // Buka Pengaturan Aplikasi (Otomatis akan berada di BAWAH layar putih kita)
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + currentApp.packageName));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);

        // Tunggu 3 Detik (Beri waktu Robot mengeklik Hapus Cache di belakang layar)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isCancelled) {
                currentIndex++;
                startCleaningLoop();
            }
        }, 3000); 
    }

    private void tutupOverlay() {
        isCancelled = true;
        // Lepaskan layar putih dari kaca HP
        if (overlayView != null && windowManager != null) {
            try {
                windowManager.removeView(overlayView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Paksa kembali ke Menu Utama aplikasimu
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tutupOverlay();
    }
}