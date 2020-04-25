package org.itoapp.strict.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.itoapp.DistanceCallback;
import org.itoapp.PublishUUIDsCallback;
import org.itoapp.TracingServiceInterface;
import org.itoapp.strict.Constants;
import org.itoapp.strict.Helper;
import org.itoapp.strict.Preconditions;
import org.itoapp.strict.database.ItoDBHelper;
import org.tcncoalition.tcnclient.bluetooth.TcnBluetoothService;
import org.tcncoalition.tcnclient.bluetooth.TcnBluetoothServiceCallback;

import java.security.SecureRandom;

public class TracingService extends TcnBluetoothService {
    private static final String LOG_TAG = "TracingService";
    private static final String DEFAULT_NOTIFICATION_CHANNEL = "ContactTracing";
    private static final int NOTIFICATION_ID = 1;
    private Looper serviceLooper;
    private Handler serviceHandler;
    private ContactCache contactCache;
    private ItoDBHelper dbHelper;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, android.content.Intent intent) {
            if (!isBluetoothRunning()) {
                startTcnExchange(new TcnBluetoothServiceCallback() {
                    @Override
                    public byte[] generateTcn() {
                        return new byte[0]; //TODO
                    }

                    @Override
                    public void onTcnFound(byte[] tcn, Double estimatedDistance) {
                        contactCache.addReceivedBroadcast(tcn, (float) estimatedDistance.doubleValue());
                    }
                });
            } else if (!Preconditions.canScanBluetooth(context)) {
                stopTcnExchange();
            }
        }
    };

    private TracingServiceInterface.Stub binder = new TracingServiceInterface.Stub() {
        @Override
        public void setDistanceCallback(DistanceCallback distanceCallback) {
            contactCache.setDistanceCallback(distanceCallback);
        }


        @Override
        public void publishBeaconUUIDs(long from, long to, PublishUUIDsCallback callback) {
            new PublishBeaconsTask(dbHelper, from, to, callback).execute();
        }

        @Override
        public boolean isPossiblyInfected() {
            //TODO do async
            long totalExposureDuration = 0;
            for (ItoDBHelper.ContactResult contact : dbHelper.selectInfectedContacts()) {
                totalExposureDuration += contact.duration;
            }
            return totalExposureDuration > Constants.MIN_EXPOSURE_DURATION;
        }

        @Override
        public void restartTracingService() {
            stopBluetooth();
            startBluetooth();
        }

        @Override
        public int getLatestFetchTime() {
            return dbHelper.getLatestFetchTime();
        }
    };
    //TODO move this to some alarmManager governed section.
// Also ideally check the server when connected to WIFI and charger
    private Runnable checkServer = () -> {
        new CheckServerTask(dbHelper).execute();
        serviceHandler.postDelayed(this.checkServer, Constants.CHECK_SERVER_INTERVAL);
    };

    private byte getTransmitPower() {
        // TODO look up transmit power for current device
        return (byte) -65;
    }

    private boolean isBluetoothRunning() {
        return false; //TODO
    }

    private void stopBluetooth() {
        //TODO
    }

    private void startBluetooth() {
        Log.i(LOG_TAG, "Starting Bluetooth");
        if (!Preconditions.canScanBluetooth(this)) {
            Log.w(LOG_TAG, "Preconditions for starting Bluetooth not met");
            return;
        }
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        assert bluetoothManager != null;
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

    }

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = new ItoDBHelper(this);
        HandlerThread thread = new HandlerThread("TracingServiceHandler", Thread.NORM_PRIORITY);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        serviceLooper = thread.getLooper();
        serviceHandler = new Handler(serviceLooper);
        serviceHandler.post(this.checkServer);
        contactCache = new ContactCache(dbHelper, serviceHandler);

        startBluetooth();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(LocationManager.MODE_CHANGED_ACTION);
        registerReceiver(broadcastReceiver, filter);
    }

    @TargetApi(26)
    private void createNotificationChannel(NotificationManager notificationManager) {
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        NotificationChannel mChannel = new NotificationChannel(DEFAULT_NOTIFICATION_CHANNEL, DEFAULT_NOTIFICATION_CHANNEL, importance);
        mChannel.enableLights(true);
        mChannel.setLightColor(Color.BLUE);
        mChannel.setImportance(NotificationManager.IMPORTANCE_LOW);
        notificationManager.createNotificationChannel(mChannel);
    }

    private void runAsForgroundService() {
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificationChannel(notificationManager);

        Intent notificationIntent = new Intent();

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this,
                DEFAULT_NOTIFICATION_CHANNEL)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationManager.IMPORTANCE_LOW)
                .setVibrate(null)
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public void onDestroy() {
        contactCache.flush();
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        runAsForgroundService();
        return START_STICKY;
    }

    /*
    Don't do anything here, because the service doesn't have to communicate to other apps
     */
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
