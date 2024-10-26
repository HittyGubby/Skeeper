package com.hittygubby.skeeper
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.app.NotificationCompat
class FloatingBarService : Service() {
    private lateinit var windowmanager: WindowManager
    private lateinit var floatingview: View
    private lateinit var floatingviewdisplay: TextView
    @SuppressLint("ForegroundServiceType", "InflateParams", "InlinedApi")
    override fun onCreate() {
        super.onCreate()
        instance = this
        windowmanager = getSystemService(WINDOW_SERVICE) as WindowManager
        floatingview = LayoutInflater.from(this).inflate(R.layout.floating_bar, null)
        floatingviewdisplay = floatingview.findViewById(R.id.floatingNumberTextView)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT)
        params.gravity = Gravity.TOP
        floatingview.setOnTouchListener(object : View.OnTouchListener {
            private var yinit: Int = 0
            private var touchyinit: Float = 0f
            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        yinit = params.y
                        touchyinit = event.rawY
                        return true }
                    MotionEvent.ACTION_MOVE -> {
                        params.y = yinit + (event.rawY - touchyinit).toInt()
                        windowmanager.updateViewLayout(floatingview, params)
                        return true } };return false }})
        windowmanager.addView(floatingview, params)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "foreground_channel",
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
            val notificationIntent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE)
            val notification = NotificationCompat.Builder(this, "foreground_channel")
                .setContentTitle("Skeeper Floatbar")
                .setContentText("Skeeper is reminding you now!!1!")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build()
            startForeground(1, notification) } }

    override fun onDestroy() { super.onDestroy();windowmanager.removeView(floatingview)}

    override fun onBind(intent: Intent): IBinder? { return null }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: FloatingBarService? = null
        fun updateFloatingBar(text: String) {
            instance?.floatingviewdisplay?.post {
                instance?.floatingviewdisplay?.text = text } } }
}

