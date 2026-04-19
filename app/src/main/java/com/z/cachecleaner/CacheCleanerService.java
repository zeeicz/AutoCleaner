package com.z.cachecleaner;

import android.accessibilityservice.AccessibilityService;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.List;

public class CacheCleanerService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) return;

        // 1. Cari tombol masuk ke menu Penyimpanan/Memori
        if (clickNodeByText(rootNode, "Penggunaan penyimpanan") || 
            clickNodeByText(rootNode, "Penggunaan Memori") || 
            clickNodeByText(rootNode, "Penyimpanan")) {
            return; // Berhenti sebentar biarkan layar memuat halaman baru
        }

        // 2. Cari tombol Hapus Cache
        if (clickNodeByText(rootNode, "Hapus cache") || 
            clickNodeByText(rootNode, "Hapus Cache") || 
            clickNodeByText(rootNode, "Bersihkan cache")) {
            
            // Jika berhasil ditekan, beri waktu setengah detik lalu paksa HP menekan tombol KEMBALI
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                performGlobalAction(GLOBAL_ACTION_BACK);
            }, 1500);
        }
    }

    // Fungsi canggih untuk mencari dan menekan teks
    private boolean clickNodeByText(AccessibilityNodeInfo node, String text) {
        List<AccessibilityNodeInfo> list = node.findAccessibilityNodeInfosByText(text);
        for (AccessibilityNodeInfo item : list) {
            CharSequence itemText = item.getText();
            // Pastikan teksnya benar-benar mirip agar tidak salah klik
            if (itemText != null && itemText.toString().toLowerCase().contains(text.toLowerCase())) {
                if (item.isClickable() && item.isEnabled()) {
                    item.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    return true;
                } else if (item.getParent() != null && item.getParent().isClickable()) {
                    item.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    return true;
                }
            }
        }
        return false; // Gagal menemukan teks
    }

    @Override
    public void onInterrupt() {}
}