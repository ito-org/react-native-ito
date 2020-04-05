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
            Log.d(LOG_TAG, "Service connected");
            tracingServiceInterface = TracingServiceInterface.Stub.asInterface(service);
            try {
                Log.d(LOG_TAG, "Registering callback");
                tracingServiceInterface.setDistanceCallback(nativeContactCallback);
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "looks like the service already crashed!", e);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d(LOG_TAG, "Service disconnected");
            tracingServiceInterface = null;
        }
    };

    private DistanceCallback.Stub nativeContactCallback = new DistanceCallback.Stub() {
        @Override
        public void onDistanceMeasurements(float[] distances) {
            try {
                if(jsDistanceCallback != null)
                    jsDistanceCallback.invoke(distances);
            }
            catch(Exception e)
            {
                Log.e(LOG_TAG, "Exception occurred invoking javascript callback", e);
                //TODO
            }
        }
    };

    public ItoBluetoothModule(ReactApplicationContext reactContext) {
        super(reactContext);
        Log.d(LOG_TAG, "Creating ItoBluetoothModule");
        this.reactContext = reactContext;
        Intent intent = new Intent(reactContext, TracingService.class);
        reactContext.startService(intent);
        bindService();
    }

    private void bindService() {
        Log.d(LOG_TAG, "binding service");
        Intent intent = new Intent(reactContext, TracingService.class);
        reactContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public String getName() {
        return "ItoBluetooth";
    }

    @ReactMethod
    public void setDistanceCallback(Callback advertisementCallback) {
        Log.d(LOG_TAG, "SetDistanceCallback called from JS");
        this.jsDistanceCallback = advertisementCallback;
    }
}
