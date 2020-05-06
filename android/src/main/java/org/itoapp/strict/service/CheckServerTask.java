package org.itoapp.strict.service;

import android.os.AsyncTask;
import android.util.Log;

import org.itoapp.strict.database.ItoDBHelper;
import org.itoapp.strict.database.RoomDB;
import org.itoapp.strict.database.entities.SeenTCN;
import org.itoapp.strict.network.NetworkHelper;

import java.util.List;

import androidx.annotation.RequiresApi;

import static org.itoapp.strict.Helper.byte2Hex;

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
        reports.stream().filter(x -> TCNProtoUtil.verifySignatureOfReportCorrect(x)).forEach(x -> TCNProtoUtil.generateAllTCNsFromReport(x, tcn -> this.checkInfection(tcn)));
        /* List<ItoDBHelper.ContactResult> contactResults = dbHelper.selectInfectedContacts();
        if (!contactResults.isEmpty()) {
            Log.w(LOG_TAG, "Possibly encountered UUIDs: " + contactResults.size());
        } */
        return null;
    }

    private void checkInfection(byte[] tcn) {
        Log.d(LOG_TAG, "Test if following TCN was seen: "  + byte2Hex(tcn));
        final SeenTCN seenTCN = RoomDB.db.seenTCNDao().findSeenTCNByHash(byte2Hex(tcn));
        if (seenTCN != null && !seenTCN.reportedSick) {
            seenTCN.reportedSick = true;
            RoomDB.db.seenTCNDao().update(seenTCN);
            Log.d(LOG_TAG, "Updated "  + seenTCN);
        }
    }
}
