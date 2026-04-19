package com.z.cachecleaner;
import android.graphics.drawable.Drawable;

public class AppInfo {
    public String appName;
    public String packageName;
    public Drawable icon;
    public long cacheSize;
    public boolean isSelectToClean; // Dicentang = Dibersihkan

    public AppInfo(String appName, String packageName, Drawable icon, long cacheSize) {
        this.appName = appName;
        this.packageName = packageName;
        this.icon = icon;
        this.cacheSize = cacheSize;
        this.isSelectToClean = true; // Defaultnya auto-select
    }
}