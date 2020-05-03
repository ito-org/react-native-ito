package org.itoapp.strict.service;

import android.os.AsyncTask;

import org.itoapp.strict.database.ItoDBHelper;
import org.itoapp.strict.network.NetworkHelper;

import java.util.List;

import androidx.annotation.RequiresApi;

import static org.itoapp.strict.Helper.encodeHexString;

public class CheckServerTask extends AsyncTask<Void, Void, Void> {
    private static final String LOG_TAG = "CheckServerTask";
    private ItoDBHelper dbHelper;

    public CheckServerTask(ItoDBHelper itoDBHelper) {
        this.dbHelper = itoDBHelper;
    }

    @RequiresApi(api = 24)
    @Override
    protected Void doInBackground(Void... voids) {
        List<byte[]> reports = NetworkHelper.refreshInfectedUUIDs();
        try {
            reports.stream().filter(x -> TCNProtoUtil.verifySignatureOfReportCorrect(x)).forEach(x -> TCNProtoUtil.generateAllTCNsFromReport(x, tcn -> this.checkInfection(tcn)));
        } catch (RuntimeException ex ) {
            ex.printStackTrace();
        }
        /* List<ItoDBHelper.ContactResult> contactResults = dbHelper.selectInfectedContacts();
        if (!contactResults.isEmpty()) {
            Log.w(LOG_TAG, "Possibly encountered UUIDs: " + contactResults.size());
        } */
        return null;
    }

    private void checkInfection(byte[] tcn) {
        System.out.println("Test if following TCN was seen: " + encodeHexString(tcn));
        // todo
    }
}
