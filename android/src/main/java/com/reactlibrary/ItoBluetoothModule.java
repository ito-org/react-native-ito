package com.reactlibrary;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.reactlibrary.service.TracingService;

public class ItoBluetoothModule extends ReactContextBaseJavaModule {

    private static final String LOG_TAG = "ItoBluetoothModule";
    private final ReactApplicationContext reactContext;
    private TracingServiceInterface tracingServiceInterface;
    private Callback jsContactCallback;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            tracingServiceInterface = TracingServiceInterface.Stub.asInterface(service);
            try {
                tracingServiceInterface.setContactCallback(nativeContactCallback);
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "looks like the service already crashed!", e);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            tracingServiceInterface = null;
        }
    };

    private ContactCallbackInterface.Stub nativeContactCallback = new ContactCallbackInterface.Stub() {
        @Override
        public void onContactFinished(byte[] broadcastData, float minDistance, long startTime, long duration) {
            jsContactCallback.invoke(broadcastData, minDistance, startTime, duration);
        }
    };

    public ItoBluetoothModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(LocationManager.MODE_CHANGED_ACTION);
        reactContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (TracingService.canServiceRun(context))
                    bindService();
                else
                    tracingServiceInterface = null;
            }
        }, filter);
    }

    private void bindService() {
        Intent intent = new Intent(reactContext, TracingService.class);
        reactContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public String getName() {
        return "ItoBluetooth";
    }

    @ReactMethod
    public void setBLEAdvertisementCallback(Callback advertisementCallback) {
        this.jsContactCallback = advertisementCallback;
    }

    @ReactMethod
    public void setAdvertisementData(ReadableArray advertisementData) {
        byte[] rawBytes = new byte[advertisementData.size()];
        for(int i = 0; i < rawBytes.length; i++) {
            rawBytes[i] = (byte)advertisementData.getInt(i);
        }

        if(tracingServiceInterface != null) {
            try {
                tracingServiceInterface.setAdvertisementData(rawBytes);
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "Could not set broadcast data!", e);
            }
        }
    }
}
