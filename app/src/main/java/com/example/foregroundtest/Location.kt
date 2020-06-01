package com.example.foregroundtest

import android.os.IBinder

data class Location(
    val lat: Double,
    val lng: Double
) : ILocation.Stub()
