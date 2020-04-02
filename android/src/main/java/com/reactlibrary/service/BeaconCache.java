package com.reactlibrary.service;

import android.os.Handler;
import android.os.RemoteException;
import android.support.v4.util.CircularArray;
import android.util.Log;


import com.reactlibrary.ContactCallbackInterface;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import okio.ByteString;

public class BeaconCache {
    private static final String LOG_TAG = "BeaconCache";
    private final int MOVING_AVERAGE_LENGTH = 7;
    private final long FLUSH_AFTER_MILLIS = 1000 * 60 * 3; // flush after three minutes

    private ContactCallbackInterface contactCallback;
    private Handler serviceHandler;
    private HashMap<ByteString, CacheEntry> cache = new HashMap<>();

    public List<NearbyDevicesListener> nearbyDevicesListeners = new ArrayList<>();

    public void setContactCallback(ContactCallbackInterface contactCallback) {
        this.contactCallback = contactCallback;
    }

    public interface NearbyDevicesListener {
        void onNearbyDevicesChanged(float[] distances);
    }

    public BeaconCache(Handler serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    private void flush(ByteString hash) {
        Log.d(LOG_TAG, "Flushing distance to DB");
        CacheEntry entry = cache.get(hash);
        try {
            contactCallback.onContactFinished(entry.hash, entry.lowestDistance, entry.firstReceived, entry.lastReceived - entry.firstReceived);
        } catch (RemoteException|NullPointerException e) {
            Log.e(LOG_TAG, "Could not store contact!");
        }
        cache.remove(hash);
    }

    public void flush() {
        for (ByteString hash : cache.keySet()) {
            flush(hash);
        }
    }

    public void addReceivedBroadcast(byte[] hash, float distance) {
        ByteString hashString = ByteString.of(hash);
        CacheEntry entry = cache.get(hashString);

        if (entry == null) {
            // new unknown broadcast
            entry = new CacheEntry();
            cache.put(hashString, entry);
            entry.hash = hash;
            entry.firstReceived = System.currentTimeMillis();
        }

        entry.lastReceived = System.currentTimeMillis();

        // postpone flushing
        serviceHandler.removeCallbacks(entry.flushRunnable);
        serviceHandler.postDelayed(entry.flushRunnable, FLUSH_AFTER_MILLIS);

        CircularArray<Float> distances = entry.distances;
        distances.addFirst(distance);
        if (distances.size() == MOVING_AVERAGE_LENGTH) {

            //calculate moving average
            float avg = entry.getAverageDistance();
            if (avg < entry.lowestDistance) {
                //insert new lowest value to DB
                entry.lowestDistance = avg;
                //insertIntoDB(hash, avg);
            }
            distances.popLast();
        }

        sendNearbyDevices();

    }

    private void sendNearbyDevices() {
        float[] nearbyDevices = getNearbyDevices();

        for (NearbyDevicesListener listener : nearbyDevicesListeners) {
            listener.onNearbyDevicesChanged(nearbyDevices);
        }
    }

    public float[] getNearbyDevices() {
        float[] nearbyDevices = new float[cache.size()];
        int i = 0;
        for (CacheEntry entry : cache.values()) {
            nearbyDevices[i] = entry.getAverageDistance();
            i++;
        }
        return nearbyDevices;
    }

    private class CacheEntry {
        long firstReceived;
        long lastReceived;
        byte[] hash;
        CircularArray<Float> distances = new CircularArray<>(MOVING_AVERAGE_LENGTH);
        float lowestDistance = Float.MAX_VALUE;
        Runnable flushRunnable = () -> flush(ByteString.of(hash));

        float getAverageDistance() {
            float avg = 0;
            for (int i = 0; i < distances.size(); i++) {
                avg += distances.get(i) / distances.size();
            }
            return avg;
        }
    }
}
