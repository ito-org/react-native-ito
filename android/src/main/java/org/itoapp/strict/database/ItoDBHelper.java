package org.itoapp.strict.database;

import android.util.Log;

import org.itoapp.strict.database.entities.LastReport;
import org.itoapp.strict.database.entities.SeenTCN;
import org.itoapp.strict.network.NetworkHelper;

import java.util.Date;

import static org.itoapp.strict.Constants.HASH_LENGTH;
import static org.itoapp.strict.Helper.encodeHexString;

public class ItoDBHelper {


    private static final String LOG_TAG = "ItoDBHelper";

    private void checkHashedUUID(byte[] hashedUUID) {

        if (hashedUUID == null || hashedUUID.length != HASH_LENGTH)
            throw new IllegalArgumentException();
    }


    public synchronized void insertContact(byte[] hashed_uuid, int proximity, long duration) {
        checkHashedUUID(hashed_uuid);
        String tcn64 = encodeHexString(hashed_uuid);
        SeenTCN seenTCN = RoomDB.db.seenTCNDao().findSeenTCNByHash(tcn64);
        if (seenTCN == null) {
            seenTCN = new SeenTCN(tcn64, new Date(), proximity, duration);
            RoomDB.db.seenTCNDao().insert(seenTCN);
        } else {
            seenTCN.lastSeen = new Date();
            seenTCN.proximity = (seenTCN.proximity + proximity) / 2;
            seenTCN.duration += duration;
            RoomDB.db.seenTCNDao().update(seenTCN);
        }
        Log.d(LOG_TAG, "Inserted contact: " + seenTCN);

    }


    public synchronized int getLatestFetchTime() {
        Log.d(LOG_TAG, "Getting latest fetch time");
        final LastReport lastReportHashForServer = RoomDB.db.lastReportDao().getLastReportHashForServer(NetworkHelper.BASE_URL);
        if (lastReportHashForServer == null)
            return 0;
        return (int) lastReportHashForServer.lastcheck.getTime()/1000; // FIXME!
    }

}
