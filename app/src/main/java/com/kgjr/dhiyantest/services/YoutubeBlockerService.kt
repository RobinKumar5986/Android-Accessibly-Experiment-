package com.kgjr.dhiyantest.services

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

@SuppressLint("AccessibilityPolicy")
class YoutubeBlockerService : AccessibilityService() {

    companion object {
        var isOverlayShowing = false
        const val ENABLE_LOGS = false
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val rootNode = rootInActiveWindow ?: return
        val packageName = rootNode.packageName?.toString() ?: return

        val supportedApps = setOf(
            "com.google.android.youtube",   // YouTube Shorts
            "com.chrome.beta",             // Chrome (for testing Shorts in browser)
            "com.android.chrome"           // Chrome stable
        )

        // Leave other apps alone
        if (packageName !in supportedApps) {
            if (isOverlayShowing) {
//            stopService(Intent(this, SimpleOverlayService::class.java))
//                isOverlayShowing = false

            }
            return
        }

        val allText = StringBuilder().apply {
            fun collect(node: AccessibilityNodeInfo?) {
                if (node == null) return
                node.text?.let { append("$it | ") }
                node.contentDescription?.let { append("$it | ") }
                for (i in 0 until node.childCount) collect(node.getChild(i))
            }
            collect(rootNode)
        }.toString()

        rootNode.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/player_view")
            .firstOrNull()?.let {
                val rect = Rect()
                it.getBoundsInScreen(rect)
                Log.d("YoutubeBlocker", "Video Player Size: ${rect.width()}x${rect.height()}")
            }

        val hasShortsRootView = rootNode.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/reel_watch_fragment_root")
            .isNotEmpty()

        val hasReelActionsContainer = rootNode.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/reel_actions_container")
            .isNotEmpty()

        val hasShortsText = allText.contains("Remix this Short") ||
                allText.contains("See more videos using this sound") ||
                (allText.contains("Remix") && allText.contains("sound", ignoreCase = true))
        val isShorts = hasShortsRootView || hasReelActionsContainer || hasShortsText

        logYoutubeDebugInfo(
            className = event.className.toString(),
            allText = allText,
            rootNode = rootNode,
            hasShortsRootView = hasShortsRootView,
            hasReelActionsContainer = hasReelActionsContainer,
            hasShortsText = hasShortsText,
            isShorts = isShorts
        )

        if (isShorts && !isOverlayShowing) {
            isOverlayShowing = true
            startService(Intent(this, SimpleOverlayService::class.java))
            performGlobalAction(GLOBAL_ACTION_BACK)
            CoroutineScope(Dispatchers.Main).launch {
                delay(3000)
                stopService(Intent(this@YoutubeBlockerService, SimpleOverlayService::class.java))
                isOverlayShowing = false

            }
        }
        else if (!isShorts && isOverlayShowing) {
//            stopService(Intent(this, SimpleOverlayService::class.java))
//            isOverlayShowing = false
        }

    }

    override fun onInterrupt() {
        if (isOverlayShowing) {
            stopService(Intent(this, SimpleOverlayService::class.java))
            isOverlayShowing = false
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
    }
    private fun logYoutubeDebugInfo(
        className: String,
        allText: String,
        rootNode: AccessibilityNodeInfo,
        hasShortsRootView: Boolean,
        hasReelActionsContainer: Boolean,
        hasShortsText: Boolean,
        isShorts: Boolean
    ) {
        if (!ENABLE_LOGS) return // Disable logs globally

        Log.d("YouTubeBlocker", "-------------------------------------")
        Log.d("YouTubeBlocker", "YOUTUBE DETECTED")
        Log.d("YouTubeBlocker", "Class Name: $className")

        // Log all collected text
        Log.d("YouTubeBlocker", "All Text: $allText")

        // Log player size
        rootNode.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/player_view")
            .firstOrNull()?.let {
                val rect = Rect()
                it.getBoundsInScreen(rect)
                Log.d("YouTubeBlocker", "Video Player Size: ${rect.width()}x${rect.height()}")
            }

        Log.d("YouTubeBlocker", "Total Nodes: ${rootNode.childCount}")

        // Shorts detection logs
        Log.d("YouTubeBlocker", "Has Shorts Root View: $hasShortsRootView")
        Log.d("YouTubeBlocker", "Has Reel Actions Container: $hasReelActionsContainer")
        Log.d("YouTubeBlocker", "Has Shorts Text: $hasShortsText")
        Log.d("YouTubeBlocker", "IS SHORTS SCREEN? → $isShorts")
        Log.d("YouTubeBlocker", "-------------------------------------\n")
    }
}