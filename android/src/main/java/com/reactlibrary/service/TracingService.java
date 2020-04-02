package com.reactlibrary.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

import com.reactlibrary.ContactCallbackInterface;
import com.reactlibrary.TracingServiceInterface;

import java.util.UUID;

public class TracingService extends Service {
    private static final String LOG_TAG = "TracingService";

    private Looper serviceLooper;
    private Handler serviceHandler;
    private BleScanner bleScanner;
    private BleAdvertiser bleAdvertiser;
    private BeaconCache beaconCache;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, android.content.Intent intent) {
            if (!canServiceRun(context)) {
                stopSelf();
            }
        }
    };

    public static boolean canServiceRun(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            //I have read this is not actually required on all devices, but I have not found a way
            //to check if it is required.
            //If location is not enabled the BLE scan fails silently (scan callback is never called)
            if (!locationManager.isLocationEnabled()) {
                Log.i(LOG_TAG, "Location not enabled (API>=P check)");
                return false;
            }
        } else {
            //Not sure if this is the correct check, gps is not really required, but passive provider
            //does not seem to be enough
            if (!locationManager.getProviders(true).contains(LocationManager.GPS_PROVIDER)) {
                Log.i(LOG_TAG, "Location not enabled (API<P check)");
                return false;
            }
        }
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        assert bluetoothManager != null;
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Log.i(LOG_TAG, "Bluetooth not enabled");
            return false;
        }
        return true;
    }

    private byte getTransmitPower() {
        // TODO look up transmit power for current device
        return (byte) -65;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread thread = new HandlerThread("TrackerHandler", Thread.NORM_PRIORITY);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        serviceLooper = thread.getLooper();
        serviceHandler = new Handler(serviceLooper);
        beaconCache = new BeaconCache(serviceHandler);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(LocationManager.MODE_CHANGED_ACTION);
        registerReceiver(broadcastReceiver, filter);



        startBluetooth();
    }

    void stopBluetooth() {
        if (bleAdvertiser != null) {
            bleAdvertiser.stopAdvertising();
            bleAdvertiser = null;
        }
        if (bleScanner != null) {
            bleScanner.stopScanning();
            bleScanner = null;
        }
    }

    void startBluetooth() {
        Log.i(LOG_TAG, "Try starting bluetooth advertisement + scanning");


        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        assert bluetoothManager != null;
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        bleScanner = new BleScanner(bluetoothAdapter, beaconCache, this);
        bleAdvertiser = new BleAdvertiser(bluetoothManager, this);

        bleAdvertiser.startAdvertising();
        bleScanner.startScanning();
    }

    @Override
    public void onDestroy() {
        stopBluetooth();
        beaconCache.flush();
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!canServiceRun(this)) {
            stopSelf(startId);
        }
        return START_STICKY;
    }

    private TracingServiceInterface.Stub binder = new TracingServiceInterface.Stub() {
        @Override
        public void setAdvertisementData(byte[] broadcastData) throws RemoteException {
            bleAdvertiser.setBroadcastData(broadcastData);
        }

        @Override
        public void setContactCallback(ContactCallbackInterface contactCallback) throws RemoteException {
            beaconCache.setContactCallback(contactCallback);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
