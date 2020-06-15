package org.itoapp.strict.service;

import android.content.Context;

import org.itoapp.strict.database.RoomDB;
import org.itoapp.strict.database.entities.LocalKey;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import androidx.annotation.RequiresApi;
import cafe.cryptography.ed25519.Ed25519PublicKey;
import cafe.cryptography.ed25519.Ed25519Signature;

import static org.itoapp.strict.Helper.byte2Hex;
import static org.itoapp.strict.Helper.hex2Byte;

public class TCNProtoUtil {

    public static boolean verifySignatureOfReportCorrect(byte[] report) {
        byte[] bsignature = Arrays.copyOfRange(report, report.length - 64, report.length);
        byte[] brvk = getRvkfromReport(report);
        byte[] breport = Arrays.copyOfRange(report, 0, report.length - 64);
        try {
            Ed25519PublicKey rvk = Ed25519PublicKey.fromByteArray(brvk);
            Ed25519Signature signature = Ed25519Signature.fromByteArray(bsignature);
            return rvk.verify(breport, signature);

        } catch (Exception e) {
            return false;
        }
    }


    public static void generateAllTCNsFromReport(byte[] report, INextTCNCallback callback) {
        int from = readUShort(report, 64);
        byte[] bstartTCK = Arrays.copyOfRange(report, 32, 64);
        TCNProtoGen ratchet = new TCNProtoGen(report[68], getRvkfromReport(report), bstartTCK, from - 1);
        int to = readUShort(report, 66);
        callback.next(ratchet.getCurrentTCN());
        for (int i = from; i < to; i++) {
            callback.next(ratchet.getNewTCN());
        }

    }


    public static void persistRatchet(Context context, TCNProtoGen ratchet) {
        RoomDB db = RoomDB.getInstance(context);
        LocalKey lk = new LocalKey();
        lk.lastGenerated = new Date();
        lk.rak = byte2Hex(ratchet.rak);
        lk.currentTCKpos = ratchet.currentTCKpos;
        db.localKeyDao().saveOrUpdate(lk);
    }

    @RequiresApi(api = 24)
    public static List<TCNProtoGen> loadAllRatchets(Context context) {
        RoomDB db = RoomDB.getInstance(context);
        return db.localKeyDao().getAll().stream().map(x -> new TCNProtoGen(hex2Byte(x.rak), x.currentTCKpos)).collect(Collectors.toList());
    }


    private static byte[] getRvkfromReport(byte[] report) {

        return Arrays.copyOfRange(report, 0, 32);
    }

    static int readUShort(byte[] report, int index) {
        final ByteBuffer bb = ByteBuffer.wrap(report);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        int i = bb.getShort(index);
        if (i < 0) {
            i = i - Short.MIN_VALUE * 2;
        }
        return i;
    }


}
