package com.z.cachecleaner;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<AppInfo> appList = new ArrayList<>();
    private AppAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        
        for (ApplicationInfo packageInfo : packages) {
            Drawable icon = packageInfo.loadIcon(pm);
            String label = packageInfo.loadLabel(pm).toString();
            
            AppInfo newApp = new AppInfo(label, packageInfo.packageName, icon, (long) (Math.random() * 50 * 1024 * 1024)); // Mock size  testing
            
            // Automatically exclude Google Play Services
            if (packageInfo.packageName.contains("com.google.android.gms")) {
                newApp.isExcluded = true;
            }
            appList.add(newApp);
        }
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