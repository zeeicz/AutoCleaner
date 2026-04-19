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

        // =========================================================
        // LANGKAH 1: MENCARI MENU PENYIMPANAN BERDASARKAN ID
        // =========================================================
        List<AccessibilityNodeInfo> storageNodes = rootNode.findAccessibilityNodeInfosByViewId("com.coloros.settings:id/oppo_preference");
        
        if (!storageNodes.isEmpty()) {
            for (AccessibilityNodeInfo node : storageNodes) {
                CharSequence text = node.getText();
                // Filter: Karena ID ini dipakai banyak menu, pastikan yang diklik ada kata "penyimpanan" atau "memori"
                if (text != null && (text.toString().toLowerCase().contains("penyimpanan") || text.toString().toLowerCase().contains("memori"))) {
                    if (node.isClickable() || (node.getParent() != null && node.getParent().isClickable())) {
                        boolean clicked = node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        if (!clicked && node.getParent() != null) {
                            node.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        }
                        return; // Berhenti di sini agar layar sempat berpindah
                    }
                }
            }
        }

        // =========================================================
        // LANGKAH 2: MENCARI TOMBOL HAPUS CACHE BERDASARKAN ID
        // =========================================================
        List<AccessibilityNodeInfo> cacheNodes = rootNode.findAccessibilityNodeInfosByViewId("com.coloros.settings:id/clear_cache_button");
        
        if (!cacheNodes.isEmpty()) {
            for (AccessibilityNodeInfo node : cacheNodes) {
                if (node.isEnabled() && node.isClickable()) {
                    // Klik tombol Hapus Cache
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    
                    // Jeda 0.5 detik (500ms) lalu tekan tombol KEMBALI (Back) di HP
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        performGlobalAction(GLOBAL_ACTION_BACK);
                    }, 500); 
                    
                    return;
                }
            }
        }
    }

    @Override
    public void onInterrupt() {
        // Biarkan kosong, dipanggil saat layanan aksesibilitas dihentikan paksa
    }
}