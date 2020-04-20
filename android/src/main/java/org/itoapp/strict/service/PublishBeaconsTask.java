package org.itoapp.strict.service;

import android.os.AsyncTask;
import android.os.RemoteException;
import android.util.Log;

import org.itoapp.PublishUUIDsCallback;
import org.itoapp.strict.database.ItoDBHelper;
import org.itoapp.strict.network.NetworkHelper;

import java.io.IOException;

class PublishBeaconsTask extends AsyncTask<Void, Void, Void> {
    private static final String LOG_TAG = "PublishBeaconsTask";
    private byte[] report;
    private long from;
    private long to;
    private PublishUUIDsCallback callback;

    public PublishBeaconsTask(byte[] report, PublishUUIDsCallback callback) {
        this.report = report;
        this.callback = callback;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            NetworkHelper.publishReport(report);
            try {
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
