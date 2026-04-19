package com.z.cachecleaner;

import android.app.AppOpsManager;
import android.app.usage.StorageStats;
import android.app.usage.StorageStatsManager;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static List<AppInfo> appList = new ArrayList<>();
    private AppAdapter adapter;
    private TextView tvTotalCache, tvTotalApps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTotalCache = findViewById(R.id.tvTotalCache);
        tvTotalApps = findViewById(R.id.tvTotalApps);
        RecyclerView rvApps = findViewById(R.id.rvApps);
        FloatingActionButton fabClean = findViewById(R.id.fabClean);

        rvApps.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppAdapter(appList);
        rvApps.setAdapter(adapter);

        loadInstalledApps();

        fabClean.setOnClickListener(v -> {
            if (!isAccessibilityServiceEnabled()) {
                Toast.makeText(this, "Aktifkan Auto Cleaner di Pengaturan Aksesibilitas", Toast.LENGTH_LONG).show();
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            } else {
                startActivity(new Intent(this, ProgressActivity.class));
            }
        });
    }

    private void loadInstalledApps() {
        if (!hasUsageStatsPermission()) {
            Toast.makeText(this, "Mohon izinkan Akses Penggunaan untuk membaca cache", Toast.LENGTH_LONG).show();
            // Arahkan user untuk memberikan izin membaca ukuran cache
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            return;
        }

        StorageStatsManager statsManager = getSystemService(StorageStatsManager.class);
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        // Menjalankan pencarian aplikasi di background agar HP tidak freeze/ngelag
        new Thread(() -> {
            long totalCacheSum = 0;
            appList.clear(); 

            for (ApplicationInfo packageInfo : packages) {
                try {
                    StorageStats stats = statsManager.queryStatsForPackage(
                            StorageStats.UUID_DEFAULT, 
                            packageInfo.packageName, 
                            android.os.Process.myUserHandle()
                    );
                    
                    long cacheSize = stats.getCacheBytes();
                    // Hanya masukkan aplikasi yang memang memiliki cache ke dalam daftar
                    if (cacheSize > 0) { 
                        AppInfo app = new AppInfo(
                            packageInfo.loadLabel(pm).toString(),
                            packageInfo.packageName,
                            packageInfo.loadIcon(pm),
                            cacheSize
                        );
                        
                        // Jangan centang Layanan Google Play secara default
                        if (packageInfo.packageName.equals("com.google.android.gms")) {
                            app.isSelectToClean = false;
                        }
                        
                        appList.add(app);
                        totalCacheSum += cacheSize;
                    }
                } catch (Exception e) { 
                    e.printStackTrace(); 
                }
            }

            long finalTotal = totalCacheSum;
            // Kembalikan ke layar utama untuk memperbarui teks
            runOnUiThread(() -> {
                tvTotalCache.setText("Total Cache: " + (finalTotal / (1024 * 1024)) + " MB");
                tvTotalApps.setText("Jumlah Aplikasi: " + appList.size());
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    private boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, 
                android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private boolean isAccessibilityServiceEnabled() {
        int accessibilityEnabled = 0;
        final String service = getPackageName() + "/" + CacheCleanerService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            return false;
        }
        
        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(':');
                splitter.setString(settingValue);
                while (splitter.hasNext()) {
                    if (splitter.next().equalsIgnoreCase(service)) return true;
                }
            }
        }
        return false;
    }
}