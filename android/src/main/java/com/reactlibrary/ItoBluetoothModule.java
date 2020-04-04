package com.reactlibrary;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import org.itoapp.DistanceCallback;
import org.itoapp.TracingServiceInterface;
import org.itoapp.strict.service.TracingService;

public class ItoBluetoothModule extends ReactContextBaseJavaModule {

    private static final String LOG_TAG = "ItoBluetoothModule";
    private final ReactApplicationContext reactContext;
    private TracingServiceInterface tracingServiceInterface;
    private Callback jsDistanceCallback;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            tracingServiceInterface = TracingServiceInterface.Stub.asInterface(service);
            try {
                tracingServiceInterface.setDistanceCallback(nativeContactCallback);
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "looks like the service already crashed!", e);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            tracingServiceInterface = null;
        }
    };

    private DistanceCallback.Stub nativeContactCallback = new DistanceCallback.Stub() {
        @Override
        public void onDistanceMeasurements(float[] distances) {
            jsDistanceCallback.invoke(distances);
        }
    };

    public ItoBluetoothModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        Intent intent = new Intent(reactContext, TracingService.class);
        reactContext.startService(intent);
        bindService();
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
    public void setDistanceCallback(Callback advertisementCallback) {
        this.jsDistanceCallback = advertisementCallback;
    }
}
