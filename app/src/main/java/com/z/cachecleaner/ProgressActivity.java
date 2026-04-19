package com.z.cachecleaner;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class ProgressActivity extends AppCompatActivity {

    private int currentIndex = 0;
    private List<AppInfo> selectedApps = new ArrayList<>();
    private boolean isCancelled = false; // Kunci Pembatalan

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // KITA KEMBALI MENGGUNAKAN LAYOUT BIASA (TANPA OVERLAY)
        setContentView(R.layout.activity_progress);

        // Tombol Batal yang sekarang 100% bisa ditekan
        Button btnCancel = findViewById(R.id.btnCancelOverlay);
        btnCancel.setOnClickListener(v -> {
            isCancelled = true;
            finish(); // Langsung keluar dari layar
        });

        // Kumpulkan aplikasi yang dicentang
        for (AppInfo app : MainActivity.appList) {
            if (app.isSelectToClean) {
                selectedApps.add(app);
            }
        }

        if (selectedApps.isEmpty()) {
            finish();
            return;
        }

        startCleaningLoop();
    }

    private void startCleaningLoop() {
        if (isCancelled || currentIndex >= selectedApps.size()) {
            finish();
            return;
        }

        AppInfo currentApp = selectedApps.get(currentIndex);

        // Update Teks dan Gambar di Layar Putih
        TextView tvIndex = findViewById(R.id.tvProgressIndex);
        tvIndex.setText("Proses " + (currentIndex + 1) + "/" + selectedApps.size());
        
        ImageView ivIcon = findViewById(R.id.ivCurrentAppIcon);
        ivIcon.setImageDrawable(currentApp.icon);
        
        TextView tvName = findViewById(R.id.tvCurrentAppName);
        tvName.setText(currentApp.appName);

        // Buka Menu Pengaturan HP (Ini akan menutupi layar putih)
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + currentApp.packageName));
        startActivity(intent);

        // Jeda 3 Detik: Memberi waktu robot untuk bekerja di Pengaturan, 
        // lalu memanggil layar putih untuk maju lagi ke depan.
        new Handler().postDelayed(() -> {
            if (!isCancelled) {
                // Tarik layar putih ke depan lagi
                Intent bringToFront = new Intent(this, ProgressActivity.class);
                bringToFront.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(bringToFront);
                
                currentIndex++;
                startCleaningLoop(); // Eksekusi aplikasi berikutnya
            }
        }, 3000); 
    }

    @Override
    public void onBackPressed() {
        // Biarkan kosong agar pengguna tidak sengaja keluar saat memencet 'Back'
    }
}