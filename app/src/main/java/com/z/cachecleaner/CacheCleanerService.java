package com.z.cachecleaner;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.List;

public class CacheCleanerService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) return;

        // Mencari tombol di layar (sesuaikan dengan bahasa HP mu)
        clickNodeByText(rootNode, "Penggunaan Memori");
        clickNodeByText(rootNode, "Storage");
        clickNodeByText(rootNode, "Hapus Cache");
        clickNodeByText(rootNode, "Clear Cache");
    }

    private void clickNodeByText(AccessibilityNodeInfo node, String text) {
        List<AccessibilityNodeInfo> list = node.findAccessibilityNodeInfosByText(text);
        for (AccessibilityNodeInfo item : list) {
            if (item.isClickable() && item.isEnabled()) {
                item.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                return;
            } else if (item.getParent() != null && item.getParent().isClickable()) {
                item.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                return;
            }
        }
    }

    @Override
    public void onInterrupt() {}
}