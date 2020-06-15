package org.itoapp.strict.service;

import android.content.Context;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.util.Log;

import org.itoapp.PublishUUIDsCallback;
import org.itoapp.strict.database.RoomDB;
import org.itoapp.strict.network.NetworkHelper;

import java.io.IOException;
import java.util.Set;

class PublishBeaconsTask extends AsyncTask<Void, Void, Void> {
    private static final String LOG_TAG = "PublishBeaconsTask";
    private final Context context;
    private Set<byte[]> report;
    private long from;
    private long to;
    private PublishUUIDsCallback callback;

    public PublishBeaconsTask(Context context, Set<byte[]> report, PublishUUIDsCallback callback) {
        this.report = report;
        this.callback = callback;
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            NetworkHelper.submitReports(report);
            RoomDB db = RoomDB.getInstance(context);
            try {
                RoomDB.db.localKeyDao().deleteAll(); // remove all Keys that we have sent
                callback.onSuccess();
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "._.", e);
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Could not publish UUIDs!", e);
            try {
                callback.onFailure();
            } catch (RemoteException ex) {
                Log.e(LOG_TAG, "._.", e);
            }
        }
        return null;
    }
}
