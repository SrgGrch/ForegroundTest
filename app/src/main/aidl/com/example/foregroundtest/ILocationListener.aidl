// ILocationListener.aidl
package com.example.foregroundtest;

import com.example.foregroundtest.ILocation;

// Declare any non-default types here with import statements

interface ILocationListener {
    oneway void onNewLocationRecieved(in Location location);
}
