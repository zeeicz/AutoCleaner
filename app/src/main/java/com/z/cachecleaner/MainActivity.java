package com.z.cachecleaner;

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
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<AppInfo> appList = new ArrayList<>();
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
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        
        long totalCacheSum = 0;
        int appCount = 0;

        // Daftar aplikasi Google bawaan yang diizinkan masuk daftar (Whitelist)
        List<String> googleWhitelist = Arrays.asList(
            "com.google.android.youtube",
            "com.android.chrome",
            "com.google.android.googlequicksearchbox", // Aplikasi Google
            "com.android.vending", // Google Play Store
            "com.google.android.apps.maps",
            "com.google.android.gms", // Layanan Google Play
            "com.google.android.gm", // Gmail
            "com.google.android.apps.photos" // Google Photos
        );

        for (ApplicationInfo packageInfo : packages) {
            boolean isSystemApp = (packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            boolean isWhitelisted = googleWhitelist.contains(packageInfo.packageName);

            // Jika itu aplikasi sistem DAN bukan bagian dari whitelist Google, abaikan/jangan tampilkan
            if (isSystemApp && !isWhitelisted) {
                continue;
            }

            Drawable icon = packageInfo.loadIcon(pm);
            String label = packageInfo.loadLabel(pm).toString();
            
            // Menggunakan ukuran acak untuk simulasi (nanti bisa dihubungkan dengan UsageStatsManager)
            long cacheSize = (long) (Math.random() * 50 * 1024 * 1024); 

            AppInfo newApp = new AppInfo(label, packageInfo.packageName, icon, cacheSize);
            
            // Logika Auto-Select
            if (packageInfo.packageName.equals("com.google.android.gms")) {
                // Khusus Layanan Google Play: TAMPIL di daftar, tapi TIDAK DICENTANG
                newApp.isSelectToClean = false;
            } else {
                // Aplikasi lain: TAMPIL di daftar dan OTOMATIS DICENTANG
                newApp.isSelectToClean = true;
            }
            
            appList.add(newApp);
            totalCacheSum += cacheSize;
            appCount++;
        }
        
        // Update teks di header atas
        tvTotalCache.setText("Total Cache: " + (totalCacheSum / (1024 * 1024)) + " MB");
        tvTotalApps.setText("Jumlah Aplikasi: " + appCount);
        
        adapter.notifyDataSetChanged();
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