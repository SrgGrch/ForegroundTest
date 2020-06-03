package com.example.foregroundtest

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat


class LocationService : Service(), LocationListener {

    private var listener: ILocationListener? = null

    private lateinit var locationManager: LocationManager
    private var mNotificationManager: NotificationManager? = null

    private var notificationInfo: NotificationInfo? = null

    private var count = 1

    private var isInited = false

    override fun onCreate() {
        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "asd"
            // Create the channel for the notification
            val mChannel =
                NotificationChannel("channel_01", name, NotificationManager.IMPORTANCE_DEFAULT)

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager!!.createNotificationChannel(mChannel)
        }

        val pendingIntent = PendingIntent.getActivity(
            baseContext,
            0,
            Intent(baseContext, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        notificationInfo = NotificationInfo(
            -1,
            createNotification(
                channelId = "channel_01",
                isOnlyAlertOnce = true,
                title = "Tracking",
                pendingIntent = pendingIntent
            )
        )

        startForeground(notificationInfo!!.id, notificationInfo!!.notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "RESTART" -> {
                if (!isInited) {

                    val prefs = getSharedPreferences("Loc", Context.MODE_PRIVATE)

                    prefs.edit()
                        .putInt("Restart", prefs.getInt("Restart", 0) + 1)
                        .apply()

                    Toast.makeText(this, "Restarted", Toast.LENGTH_LONG).show()
                    start()
                }
            }

            "STOP" -> {
                isInited = false
                stopForeground(true)
                locationManager.removeUpdates(this)
                stopSelf()
            }
            "START" -> {
                start()
            }
        }

        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(this, AlarmReceiver::class.java).apply {
            action = "RESTART"
        }.let {
            PendingIntent.getBroadcast(this, 0, it, 0)
        }

        alarmManager.cancel(alarmIntent)

        Toast.makeText(this, "Перезапуск остановлен", Toast.LENGTH_LONG).show()
        super.onTaskRemoved(rootIntent)
    }

    private fun start() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(this, AlarmReceiver::class.java).apply {
            action = "RESTART"
        }.let {
            PendingIntent.getBroadcast(this, 0, it, 0)
        }


        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + 60 * 1000,
            alarmIntent
        )
//        alarmManager.setRepeating(
//            AlarmManager.ELAPSED_REALTIME_WAKEUP,
//            SystemClock.elapsedRealtime() + 60 * 1000,
//            60 * 1000,
//            alarmIntent
//        )

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val provider = LocationManager.GPS_PROVIDER
        val providers = locationManager.getProviders(false)

        providers.map {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                locationManager.requestLocationUpdates(it, 0L, 0f, this, Looper.getMainLooper())
            }
        }

        isInited = true
    }

    override fun onBind(intent: Intent?): IBinder? {

//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            locationManager.requestLocationUpdates(provider, 0L, 0f, this, Looper.getMainLooper())
//        }

        return binder
    }

    override fun stopService(name: Intent?): Boolean {
        return super.stopService(name)
    }

    private val binder = object : ILocationService.Stub() {
        override fun addOnNewLocationListener(listener: ILocationListener?) {
            listener?.let { this@LocationService.listener = listener }
        }

        override fun removeOnNewLocationListener() {
            listener = null
        }
    }

    override fun onLocationChanged(location: Location?) {
        notificationInfo?.let {
            val pendingIntent = PendingIntent.getActivity(
                baseContext,
                0,
                Intent(baseContext, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            val prefs = getSharedPreferences("Loc", Context.MODE_PRIVATE)
            val restartCount = prefs.getInt("Restart", 0)

            mNotificationManager!!.notify(
                it.id, createNotification(
                    channelId = "channel_01",
                    isOnlyAlertOnce = true,
                    title = "${location?.latitude} : ${location?.longitude}, ${count++}. R: $restartCount",
                    pendingIntent = pendingIntent
                )
            )
        }
        listener?.onNewLocationRecieved(location)
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        Log.w("Service", "onStatusChanged")
    }

    override fun onProviderEnabled(provider: String?) {
        Log.w("Service", "onProviderEnabled")
    }

    override fun onProviderDisabled(provider: String?) {
        Log.w("Service", "onProviderDisabled")
    }

    private fun createNotification(
        channelId: String,
        isOnlyAlertOnce: Boolean = false,
        title: String? = null,
        text: String? = null,
        priority: Int = NotificationCompat.PRIORITY_DEFAULT,
        pendingIntent: PendingIntent? = null,
        isAutoCancelable: Boolean = false
    ) = NotificationCompat.Builder(baseContext, channelId).apply {

        pendingIntent?.let { setContentIntent(it) }
        title?.let { setContentTitle(it) }
        text?.let { setContentText(it) }

        setOnlyAlertOnce(isOnlyAlertOnce)
        setAutoCancel(isAutoCancelable)

        setSmallIcon(R.drawable.ic_launcher_foreground)

        color = 0

        // For android 7.1 and lower
        this.priority = priority
        setDefaults(NotificationCompat.DEFAULT_ALL)
    }.build()

    class NotificationInfo(val id: Int, val notification: Notification)
}