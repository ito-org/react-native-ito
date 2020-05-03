package org.itoapp.strict.service;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import cafe.cryptography.curve25519.InvalidEncodingException;
import cafe.cryptography.ed25519.Ed25519PublicKey;
import cafe.cryptography.ed25519.Ed25519Signature;

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
        //System.out.println("reading from " + from + " to " + to);
        callback.next(ratchet.getCurrentTCN());
        for (int i = from; i < to; i++) {
            callback.next(ratchet.getNewTCN());
        }

    }


    private static byte[] getRvkfromReport(byte[] report) {

        return Arrays.copyOfRange(report, 0, 32);
    }

    static int readUShort(byte[] report, int index) {
        final ByteBuffer bb = ByteBuffer.wrap(report);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        int i = bb.getShort(index);
        if (i < 0) {
            i =  i - Short.MIN_VALUE *2;
        }
        return i;
    }
}