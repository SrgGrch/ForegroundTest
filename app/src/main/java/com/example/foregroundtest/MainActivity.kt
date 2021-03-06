package com.example.foregroundtest

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    var locationService: ILocationService? = null
    var count = 1

    private val mConnection = object : ServiceConnection {
        // Called when the connection with the service is established
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // Following the example above for an AIDL interface,
            // this gets an instance of the IRemoteInterface, which we can use to call on the service
            locationService = ILocationService.Stub.asInterface(service)
            locationService!!.addOnNewLocationListener(object : ILocationListener.Stub() {
                override fun onNewLocationRecieved(location: Location?) {
                    textView.post {
                        textView.text = "${location?.latitude} : ${location?.longitude}"
                        textView2.text = count++.toString()
                    }
                }
            })
        }

        // Called when the connection with the service disconnects unexpectedly
        override fun onServiceDisconnected(className: ComponentName) {
            Log.e("", "Service has unexpectedly disconnected")
            locationService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startButton.setOnClickListener {

            val prefs = getSharedPreferences("Loc", Context.MODE_PRIVATE)

            prefs.edit()
                .putInt("Restart", 0)
                .apply()

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val intent = Intent(this, LocationService::class.java).apply {
                    action = "START"
                }

                startService(intent)
                bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
                startButton.isEnabled = false
                stopButton.isEnabled = true

                textView.text = "Binding"
            } else {
                requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    1
                )
            }

        }

        stopButton.setOnClickListener {
            if (locationService != null) {
                locationService?.removeOnNewLocationListener()
                unbindService(mConnection)
                startService(Intent(this, LocationService::class.java).apply {
                    action = "STOP"
                })
                startButton.isEnabled = true
                stopButton.isEnabled = false
                textView.text = "Stopped"
                count = 0
                textView2.text = count.toString()
            }

            val prefs = getSharedPreferences("Loc", Context.MODE_PRIVATE)

            prefs.edit()
                .putInt("Restart", 0)
                .apply()
        }

        stopAlarmButton.setOnClickListener {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val alarmIntent = Intent(this, AlarmReceiver::class.java).apply {
                action = "RESTART"
            }.let {
                PendingIntent.getBroadcast(this, 0, it, 0)
            }

            alarmManager.cancel(alarmIntent)

            Toast.makeText(this, "Перезапуск остановлен", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        startService(Intent(this, LocationService::class.java).apply {
            action = "STOP"
        })

        super.onDestroy()
    }
}
