// TracingServiceInterface.aidl
package com.reactlibrary;

import com.reactlibrary.ContactCallbackInterface;

// Declare any non-default types here with import statements

interface TracingServiceInterface {
    void setAdvertisementData(in byte[] broadcastData);

    // null to unset
    void setContactCallback(ContactCallbackInterface contactCallback);
}
