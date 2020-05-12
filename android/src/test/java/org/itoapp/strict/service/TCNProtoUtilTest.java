package org.itoapp.strict.service;

import org.itoapp.strict.database.RoomDB;
import org.itoapp.strict.database.dao.LastReportDao;
import org.itoapp.strict.database.dao.LocalKeyDao;
import org.itoapp.strict.database.dao.SeenTCNDao;
import org.itoapp.strict.database.entities.LocalKey;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

import static org.itoapp.strict.service.TCNProtoGenTest.REPORTTCNS;
import static org.itoapp.strict.Helper.byte2Hex;
import static org.itoapp.strict.Helper.hex2Byte;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TCNProtoUtilTest {


    @Test
    public void testVerifySig() throws Exception {
        byte[] report = hex2Byte(TCNProtoGenTest.VALIDREPORT);
        assertEquals(true, TCNProtoUtil.verifySignatureOfReportCorrect(report));

    }

    @Test
    public void generateFromTo() throws Exception {
        byte[] report = hex2Byte(TCNProtoGenTest.VALIDREPORT);
        List<String> tcns = new LinkedList<>();
        TCNProtoUtil.generateAllTCNsFromReport(report, x ->
                tcns.add(byte2Hex(x))
        );
        assertArrayEquals(REPORTTCNS, tcns.toArray());
    }

    @Test // not too much of a unit test
    public void testBounds() throws Exception {
        TCNProtoGen tcnGen = new TCNProtoGen();
        int ulimit = Short.MAX_VALUE * 2;
        List<String> tcnsSource = new LinkedList<>();
        IntStream.range(0, ulimit).forEach(i
                -> tcnsSource.add(byte2Hex(tcnGen.getNewTCN())));

        List<String> tcnRegenerated = new LinkedList<>();
        TCNProtoUtil.generateAllTCNsFromReport(tcnGen.generateReport(tcnGen.getRatchetTickCount()), x ->
                tcnRegenerated.add(byte2Hex(x))
        );
        assertEquals(ulimit, tcnRegenerated.size());
        assertArrayEquals(tcnsSource.toArray(), tcnRegenerated.toArray());
    }


    @Test
    public void testSaveAndLoad() throws Exception {

        // 0 Mock DB
        Map<String, LocalKey> lk = new HashMap<>();
        RoomDB.db = new RoomDB() {
            @Override
            public LastReportDao lastReportDao() {
                return null;
            }

            @Override
            public SeenTCNDao seenTCNDao() {
                return null;
            }

            @Override
            public LocalKeyDao localKeyDao() {
                return new LocalKeyDao() {


                    @Override
                    public List<LocalKey> getAll() {
                        return new ArrayList(lk.values());
                    }

                    @Override
                    public void saveOrUpdate(LocalKey localKey) {
                        lk.put(localKey.rak, localKey);
                    }

                    @Override
                    public void deleteAll() {
                        lk.clear();
                    }
                };
            }

            @NonNull
            @Override
            protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration config) {
                return null;
            }

            @NonNull
            @Override
            protected InvalidationTracker createInvalidationTracker() {
                return null;
            }

            @Override
            public void clearAllTables() {

            }
        };


        // 1) Setup Sample data
        // 10 Days with 96 TCN's per Day
        Set<String> tcnsSource = new HashSet<>();
        IntStream.range(0, 10).forEach(i
                -> {
            TCNProtoGen tcnGen = new TCNProtoGen();
            IntStream.range(0, 96).forEach(i2
                    -> {
                tcnsSource.add(byte2Hex(tcnGen.getNewTCN()));
                TCNProtoUtil.persistRatchet(tcnGen);
            });
        });

        // 2) verify every TCN is found in the reports
        TCNProtoUtil.loadAllRatchets().stream().map(ratchet ->
                ratchet.generateReport(ratchet.getRatchetTickCount()))
                .filter(x -> TCNProtoUtil.verifySignatureOfReportCorrect(x)).forEach(x -> TCNProtoUtil.generateAllTCNsFromReport(x, tcn -> tcnsSource.remove(byte2Hex(tcn))));
        assertEquals(0, tcnsSource.size());

    }
}
