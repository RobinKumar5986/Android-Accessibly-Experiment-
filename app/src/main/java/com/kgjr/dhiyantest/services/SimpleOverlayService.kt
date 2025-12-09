package com.kgjr.dhiyantest.services

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.TextView

class SimpleOverlayService : Service() {

    private var overlayView: View? = null
    private var wm: WindowManager? = null

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        val view = TextView(this).apply {
            text = "YOUTUBE BLOCKED\n\nTouch grass"
            textSize = 44f
            gravity = Gravity.CENTER
            setBackgroundColor(0xCC000000.toInt())
            setTextColor(0xFFFF4444.toInt())
        }

        overlayView = view
        wm?.addView(view, params)
    }

    override fun onDestroy() {
        overlayView?.let { wm?.removeView(it) }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}