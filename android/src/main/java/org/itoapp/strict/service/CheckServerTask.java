package org.itoapp.strict.service;

import android.content.Context;
import android.os.AsyncTask;

import org.itoapp.strict.database.RoomDB;
import org.itoapp.strict.network.NetworkHelper;

import java.util.Set;
import java.util.stream.Collectors;

import static org.itoapp.strict.Helper.hex2Byte;

public class CheckServerTask extends AsyncTask<Void, Void, Integer> {
    private static final String LOG_TAG = "ITOCheckServerTask";
    private final CheckServerTaskCallback callback;
    private final Context context;


    public CheckServerTask(Context context, CheckServerTaskCallback callback) {
        this.callback = callback;
        this.context = context;
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        try {
            // TODO employ a more sophisticated risk-scoring algorithm
            // This is where low-risk contacts could be filtered out
            // or queried separately
            RoomDB db = RoomDB.getInstance(context);

            Set<byte[]> contactTCNs = db.seenTCNDao().getAllSeenTCNs()
                    .stream()
                    .map(seenTCN -> hex2Byte(seenTCN.tcn))
                    .collect(Collectors.toSet());

            return NetworkHelper.getNumberOfInfectedContacts(contactTCNs);
        } catch (Exception ex) {
            ex.printStackTrace(); // FIXME: Notify user of failed update
        }
        return null;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        if (integer != null)
            callback.riskCalculated(integer);
    }

    public interface CheckServerTaskCallback {
        void riskCalculated(int risk);
    }
}
