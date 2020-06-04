package com.example.foregroundtest

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.util.Log
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

    override fun onDestroy() {
        startService(Intent(this, LocationService::class.java).apply {
            action = "STOP"
        })

        unbindService(mConnection)
        super.onDestroy()
    }
}
