package com.z.cachecleaner;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class ProgressActivity extends AppCompatActivity {
    private int currentIndex = 0;
    private List<AppInfo> selectedApps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        // Ambil daftar aplikasi yang dicentang dari Intent atau Singleton
        selectedApps = getAppsToClean(); 
        startCleaningLoop();
    }

    private void startCleaningLoop() {
        if (currentIndex >= selectedApps.size()) {
            finish(); // Selesai
            return;
        }

        AppInfo currentApp = selectedApps.get(currentIndex);

        // Update UI
        TextView tvIndex = findViewById(R.id.tvProgressIndex);
        tvIndex.setText("Proses " + (currentIndex + 1) + "/" + selectedApps.size());
        
        ImageView ivIcon = findViewById(R.id.ivCurrentAppIcon);
        ivIcon.setImageDrawable(currentApp.icon);
        
        TextView tvName = findViewById(R.id.tvCurrentAppName);
        tvName.setText(currentApp.appName);

        // Buka Halaman Settings Aplikasi
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + currentApp.packageName));
        startActivity(intent);

        // Tunggu 2 detik (memberi waktu robot aksesibilitas bekerja) lalu lanjut ke aplikasi berikutnya
        new Handler().postDelayed(() -> {
            currentIndex++;
            startCleaningLoop();
        }, 2500); 
    }
}