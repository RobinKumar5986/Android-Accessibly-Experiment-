package com.kgjr.dhiyantest.services

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

@SuppressLint("AccessibilityPolicy")
class YoutubeBlockerService : AccessibilityService() {

    companion object {
        var isOverlayShowing = false
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val rootNode = rootInActiveWindow ?: return
        val packageName = rootNode.packageName?.toString() ?: return

        val isYoutube = packageName == "com.google.android.youtube"

        if (isYoutube && !isOverlayShowing) {
            startService(Intent(this, SimpleOverlayService::class.java))
            isOverlayShowing = true
        } else if (!isYoutube && isOverlayShowing) {
            stopService(Intent(this, SimpleOverlayService::class.java))
            isOverlayShowing = false
        }
    }

    override fun onInterrupt() {
        if (isOverlayShowing) {
            stopService(Intent(this, SimpleOverlayService::class.java))
            isOverlayShowing = false
        }
    }
}