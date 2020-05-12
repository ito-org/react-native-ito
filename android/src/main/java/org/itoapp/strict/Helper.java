package org.itoapp.strict;

import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static org.itoapp.strict.Constants.HASH_LENGTH;

public class Helper {

    private static final String LOG_TAG = "ITOHelper";
    private static MessageDigest sha256MessageDigest;

    private Helper() {
    }

    public static byte[] calculateTruncatedSHA256(byte[] uuid) {
        if(sha256MessageDigest == null) {
            try {
                sha256MessageDigest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                Log.wtf(LOG_TAG, "Algorithm not found", e);
                throw new RuntimeException(e);
            }
        }

        byte[] sha256Hash = sha256MessageDigest.digest(uuid);
        return Arrays.copyOf(sha256Hash, HASH_LENGTH);
    }

    public static byte[] hex2Byte(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static String byte2Hex(byte[] in) {
        String s = "";
        for (byte b : in) {
            String st = String.format("%02X", b).toLowerCase();
            s += st;
        }
        return s;
    }


}
