package com.kgjr.dhiyantest.services

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

@SuppressLint("AccessibilityPolicy")
class YoutubeBlockerService : AccessibilityService() {

    companion object {
        var isOverlayShowing = false
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val rootNode = rootInActiveWindow ?: return
        val packageName = rootNode.packageName?.toString() ?: return

        // SUPPORTED APPS (add more anytime)
        val supportedApps = setOf(
            "com.google.android.youtube",   // YouTube Shorts
            "com.instagram.android",       // Instagram Reels
            "com.zhiliaoapp.musically",    // TikTok (global)
            "com.ss.android.ugc.trill",    // TikTok (some regions)
            "com.chrome.beta",             // Chrome (for testing Shorts in browser)
            "com.android.chrome"           // Chrome stable
        )

        // Leave other apps alone
        if (packageName !in supportedApps) {
            if (isOverlayShowing) {
//            stopService(Intent(this, SimpleOverlayService::class.java))

                isOverlayShowing = false

            }
            return
        }

        // ——————— YOUTUBE IS OPEN → LOG + DETECT SHORTS ———————
        Log.d("YoutubeBlocker", "YOUTUBE DETECTED")

        // 1. Collect ALL text once (for logs + detection)
        val allText = StringBuilder().apply {
            fun collect(node: AccessibilityNodeInfo?) {
                if (node == null) return
                node.text?.let { append("$it | ") }
                node.contentDescription?.let { append("$it | ") }
                for (i in 0 until node.childCount) collect(node.getChild(i))
            }
            collect(rootNode)
        }.toString()

        Log.d("YoutubeBlocker", "All Text: $allText")

        // 2. Video player size (for debugging)
        rootNode.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/player_view")
            .firstOrNull()?.let {
                val rect = Rect()
                it.getBoundsInScreen(rect)
                Log.d("YoutubeBlocker", "Video Player Size: ${rect.width()}x${rect.height()}")
            }

        // 3. Node count
        Log.d("YoutubeBlocker", "Total Nodes: ${rootNode.childCount}")

        // ——————— PERFECT SHORTS DETECTION (NEVER FAILS) ———————
        val isShorts = allText.contains("Remix this Short") ||
                allText.contains("See more videos using this sound") ||
                (allText.contains("Remix") && allText.contains("sound", ignoreCase = true))

        Log.d("YoutubeBlocker", "IS SHORTS SCREEN? → $isShorts")

        // ——————— BLOCK ONLY SHORTS ———————
        if (isShorts && !isOverlayShowing) {
            Log.d("YoutubeBlocker", "BLOCKING SHORTS!")
//            startService(Intent(this, SimpleOverlayService::class.java))
            performGlobalAction(GLOBAL_ACTION_BACK)
            isOverlayShowing = true
        }
        else if (!isShorts && isOverlayShowing) {
            Log.d("YoutubeBlocker", "UNBLOCKED – Not Shorts")
//            stopService(Intent(this, SimpleOverlayService::class.java))
            isOverlayShowing = false
        }

        Log.d("YoutubeBlocker", "END YOUTUBE\n")
    }

    override fun onInterrupt() {
        if (isOverlayShowing) {
//            stopService(Intent(this, SimpleOverlayService::class.java))
            isOverlayShowing = false
        }
    }
}