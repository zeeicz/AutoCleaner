package com.z.cachecleaner; // Sesuaikan dengan namamu

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class ProgressActivity extends AppCompatActivity {
    
    private int currentIndex = 0;
    private List<AppInfo> selectedApps = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        // Mengambil daftar aplikasi dari MainActivity yang statusnya "dicentang"
        for (AppInfo app : MainActivity.appList) {
            if (app.isSelectToClean) {
                selectedApps.add(app);
            }
        }

        // Jika pengguna tidak mencentang aplikasi satupun, langsung tutup layar
        if (selectedApps.isEmpty()) {
            finish();
            return;
        }

        startCleaningLoop();
    }

    private void startCleaningLoop() {
        if (currentIndex >= selectedApps.size()) {
            finish(); // Selesai semua
            return;
        }

        AppInfo currentApp = selectedApps.get(currentIndex);

        // Update teks "Proses 1/50"
        TextView tvIndex = findViewById(R.id.tvProgressIndex);
        tvIndex.setText("Proses " + (currentIndex + 1) + "/" + selectedApps.size());
        
        // Update Gambar Ikon
        ImageView ivIcon = findViewById(R.id.ivCurrentAppIcon);
        ivIcon.setImageDrawable(currentApp.icon);
        
        // Update Nama Aplikasi
        TextView tvName = findViewById(R.id.tvCurrentAppName);
        tvName.setText(currentApp.appName);

        // Buka Halaman Pengaturan Aplikasi tersebut
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + currentApp.packageName));
        startActivity(intent);

        // Tunggu 2,5 detik (beri waktu robot bekerja), lalu lanjut ke aplikasi berikutnya
        new Handler().postDelayed(() -> {
            currentIndex++;
            startCleaningLoop();
        }, 2500); 
    }

    @Override
    public void onBackPressed() {
        // Biarkan kosong agar tombol 'Back' di HP terblokir saat proses berjalan
    }
}