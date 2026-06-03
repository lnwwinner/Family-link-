package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.Toast
import com.example.R

class FamilyCareAccessibilityService : AccessibilityService() {

    private var windowManager: WindowManager? = null
    private var floatingButtonView: View? = null
    private val handler = Handler(Looper.getMainLooper())
    
    // Tracking volume key long press durations
    private var volumeUpDownTime: Long = 0
    private var volumeUpDownPressing = false
    private var volumeDownDownTime: Long = 0
    private var volumeDownDownPressing = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i("AccessibilityService", "FamilyCareAccessibilityService Connected and Listening")
        Toast.makeText(this, "Family Care Security Watchdog Service Connected", Toast.LENGTH_SHORT).show()
        setupFloatingSosButton()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Essential override for accessibility tracking
    }

    override fun onInterrupt() {
        Log.i("AccessibilityService", "FamilyCareAccessibilityService Process Interrupted")
        removeFloatingSosButton()
    }

    override fun onDestroy() {
        super.onDestroy()
        removeFloatingSosButton()
    }

    // Capture volume hardware button events
    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event == null) return false
        val keyCode = event.keyCode
        val action = event.action

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (action == KeyEvent.ACTION_DOWN) {
                if (!volumeUpDownPressing) {
                    volumeUpDownPressing = true
                    volumeUpDownTime = System.currentTimeMillis()
                    handler.postDelayed(volumeUpLongPressRunnable, 3000) // 3 seconds trigger
                }
                return true // Consume key
            } else if (action == KeyEvent.ACTION_UP) {
                volumeUpDownPressing = false
                handler.removeCallbacks(volumeUpLongPressRunnable)
                return true
            }
        }

        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (action == KeyEvent.ACTION_DOWN) {
                if (!volumeDownDownPressing) {
                    volumeDownDownPressing = true
                    volumeDownDownTime = System.currentTimeMillis()
                    handler.postDelayed(volumeDownLongPressRunnable, 3000) // 3 seconds trigger
                }
                return true
            } else if (action == KeyEvent.ACTION_UP) {
                volumeDownDownPressing = false
                handler.removeCallbacks(volumeDownLongPressRunnable)
                return true
            }
        }

        return super.onKeyEvent(event)
    }

    private val volumeUpLongPressRunnable = Runnable {
        if (volumeUpDownPressing) {
            Log.w("AccessibilityService", "Hardware hotkey: Volume Up long press SOS triggered!")
            Toast.makeText(this, "HOTKEY TRIPPED: VOLUME UP LONG PRESS -> DISPATCHING SOS NOW", Toast.LENGTH_LONG).show()
            
            // Broadcast intent to trigger automatic alarm sequence inside application
            val intent = Intent("com.example.ACTION_HARDWARE_SOS").apply {
                putExtra("source", "Volume Up Long Press")
                setPackage(packageName)
            }
            sendBroadcast(intent)
            volumeUpDownPressing = false
        }
    }

    private val volumeDownLongPressRunnable = Runnable {
        if (volumeDownDownPressing) {
            Log.w("AccessibilityService", "Hardware hotkey: Volume Down long press -> CAREGIVER COORD!")
            Toast.makeText(this, "HOTKEY TRIPPED: DIRECT CALL TO PRIMARY CAREGIVER", Toast.LENGTH_LONG).show()
            
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:0812345678")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            volumeDownDownPressing = false
        }
    }

    // Floating SOS button rendering (drawn on top of all application views)
    private fun setupFloatingSosButton() {
        try {
            windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            
            // Re-using simplified view code or building a clean native circular view programmatically
            val context = this
            val button = Button(context).apply {
                text = "SOS"
                setTextColor(android.graphics.Color.WHITE)
                setBackgroundColor(android.graphics.Color.RED)
                textSize = 14f
                val dip = 64f
                val px = (dip * context.resources.displayMetrics.density).toInt()
                width = px
                height = px
                setOnClickListener {
                    Log.d("FloatingSos", "One Touch Floating Button Clicked!")
                    Toast.makeText(context, "FLOATING BUTTON PRESS: BROADCASTING SOS ALARM", Toast.LENGTH_LONG).show()
                    val intent = Intent("com.example.ACTION_HARDWARE_SOS").apply {
                        putExtra("source", "Floating SOS Button")
                        setPackage(context.packageName)
                    }
                    sendBroadcast(intent)
                }
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                },
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 100
                y = 300
            }

            // Simple touch-dragging simulation helper
            button.setOnTouchListener(object : View.OnTouchListener {
                private var initialX = 0
                private var initialY = 0
                private var initialTouchX = 0f
                private var initialTouchY = 0f

                override fun onTouch(v: View?, event: android.view.MotionEvent?): Boolean {
                    if (event == null) return false
                    when (event.action) {
                        android.view.MotionEvent.ACTION_DOWN -> {
                            initialX = params.x
                            initialY = params.y
                            initialTouchX = event.rawX
                            initialTouchY = event.rawY
                            return true
                        }
                        android.view.MotionEvent.ACTION_MOVE -> {
                            params.x = initialX + (event.rawX - initialTouchX).toInt()
                            params.y = initialY + (event.rawY - initialTouchY).toInt()
                            windowManager?.updateViewLayout(button, params)
                            return true
                        }
                        android.view.MotionEvent.ACTION_UP -> {
                            val diffX = Math.abs(event.rawX - initialTouchX)
                            val diffY = Math.abs(event.rawY - initialTouchY)
                            if (diffX < 10 && diffY < 10) {
                                v?.performClick()
                            }
                            return true
                        }
                    }
                    return false
                }
            })

            floatingButtonView = button
            windowManager?.addView(button, params)
            Log.i("AccessibilityService", "Floating SOS trigger added successfully onto viewport")
        } catch (e: Exception) {
            Log.e("AccessibilityService", "Failed to render floating SOS layout: ${e.message}", e)
        }
    }

    private fun removeFloatingSosButton() {
        floatingButtonView?.let {
            try {
                windowManager?.removeView(it)
                floatingButtonView = null
            } catch (e: Exception) {
                Log.e("AccessibilityService", "Failed to remove float view: ${e.message}")
            }
        }
    }
}

// Simulated simple URI parser mapping internal dependencies
private object Uri {
    fun parse(uriString: String): android.net.Uri = android.net.Uri.parse(uriString)
}
