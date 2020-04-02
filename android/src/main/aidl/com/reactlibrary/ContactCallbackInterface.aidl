// ContactCallbackInterface.aidl
package com.reactlibrary;

// Declare any non-default types here with import statements

interface ContactCallbackInterface {
    void onContactFinished(in byte[] broadcastData,  float minDistance, long startTime, long duration);
}
