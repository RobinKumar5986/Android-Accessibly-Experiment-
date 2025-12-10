package com.kgjr.dhiyantest.services

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView

class SimpleOverlayService : Service() {

    private var overlayView: View? = null
    private var wm: WindowManager? = null

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager

        val screenHeight = resources.displayMetrics.heightPixels
        val desiredHeight = (screenHeight * 0.5f).toInt() // bottom 50%

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            desiredHeight,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.BOTTOM

        // ----- Container Layout -----
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(40, 60, 40, 60)

            // solid background with rounded top corners
            background = GradientDrawable().apply {
                setColor(0xFF1E1E1E.toInt())
                cornerRadii = floatArrayOf(
                    50f, 50f,
                    50f, 50f,
                    0f, 0f,
                    0f, 0f
                )
            }
        }

        // ----- Message -----
        val message = TextView(this).apply {
            text = "YOUTUBE BLOCKED\nGo Touch Some Grass 🌱"
            textSize = 26f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
        }

        container.addView(message)

        overlayView = container
        wm?.addView(container, params)
//        Handler(mainLooper).postDelayed({
//            stopSelf()
//        }, 3000)
    }

    override fun onDestroy() {
        overlayView?.let { wm?.removeView(it) }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
