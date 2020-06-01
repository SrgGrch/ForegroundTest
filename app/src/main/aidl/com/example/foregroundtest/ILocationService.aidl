// ILocationService.aidl
package com.example.foregroundtest;


import com.example.foregroundtest.ILocationListener;

// Declare any non-default types here with import statements

interface ILocationService {
    void addOnNewLocationListener(in ILocationListener listener);
    void removeOnNewLocationListener();
}
