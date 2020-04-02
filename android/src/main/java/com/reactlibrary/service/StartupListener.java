package com.reactlibrary.service;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

/*
This BroadcastReceiver starts the tracing service when the system boots
 */
public class StartupListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //try starting the service. If not all requirements are met, it won't start
        Intent serviceIntent = new Intent(context, TracingService.class);
        context.startService(serviceIntent);
    }
}
