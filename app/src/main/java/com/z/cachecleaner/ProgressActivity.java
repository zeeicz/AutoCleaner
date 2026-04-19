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
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class ProgressActivity extends AppCompatActivity {
    
    private int currentIndex = 0;
    private List<AppInfo> selectedApps = new ArrayList<>();
    
    // Variabel untuk Overlay
    private WindowManager windowManager;
    private View overlayView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // KITA TIDAK MENGGUNAKAN setContentView() LAGI!
        // Kita mencetak layout langsung ke kaca layar HP (WindowManager)

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        overlayView = LayoutInflater.from(this).inflate(R.layout.activity_progress, null);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, // Standar Android 8 ke atas
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, // Agar tembus pandang untuk sentuhan robot
                PixelFormat.TRANSLUCENT
        );

        // Pasang Layar Putih ke depan wajah pengguna
        windowManager.addView(overlayView, params);

        // Ambil daftar aplikasi dari MainActivity
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
        if (currentIndex >= selectedApps.size()) {
            tutupOverlay(); // Selesai semua
            return;
        }

        AppInfo currentApp = selectedApps.get(currentIndex);

        // UPDATE UI: Perhatikan kita mengambil elemen dari 'overlayView', bukan dari activity
        TextView tvIndex = overlayView.findViewById(R.id.tvProgressIndex);
        tvIndex.setText("Proses " + (currentIndex + 1) + "/" + selectedApps.size());
        
        ImageView ivIcon = overlayView.findViewById(R.id.ivCurrentAppIcon);
        ivIcon.setImageDrawable(currentApp.icon);
        
        TextView tvName = overlayView.findViewById(R.id.tvCurrentAppName);
        tvName.setText(currentApp.appName);

        // Buka Pengaturan Aplikasi di belakang layar putih
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + currentApp.packageName));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Pastikan terbuka sebagai tugas baru
        startActivity(intent);

        // Tunggu 2,5 detik agar robot sempat mencari dan mengklik, lalu lanjut!
        new Handler().postDelayed(() -> {
            currentIndex++;
            startCleaningLoop();
        }, 2500); 
    }

    private void tutupOverlay() {
        // Cabut layar putih dari kaca HP
        if (overlayView != null && windowManager != null) {
            try {
                windowManager.removeView(overlayView);
            } catch (Exception e) { e.printStackTrace(); }
        }
        
        // Panggil kembali layar utama
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Jaga-jaga jika activity mati paksa, hapus overlay-nya
        if (overlayView != null && overlayView.getWindowToken() != null) {
            windowManager.removeView(overlayView);
        }
    }

    @Override
    public void onBackPressed() {
        // Blokir tombol kembali saat proses berlangsung
    }
}