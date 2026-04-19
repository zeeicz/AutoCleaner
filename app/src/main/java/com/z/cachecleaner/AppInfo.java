package com.z.cachecleaner;
import android.graphics.drawable.Drawable;

public class AppInfo {
    public String appName;
    public String packageName;
    public Drawable icon;
    public long cacheSize;
    public boolean isExcluded;

    public AppInfo(String appName, String packageName, Drawable icon, long cacheSize) {
        this.appName = appName;
        this.packageName = packageName;
        this.icon = icon;
        this.cacheSize = cacheSize;
        this.isExcluded = false;
    }
}