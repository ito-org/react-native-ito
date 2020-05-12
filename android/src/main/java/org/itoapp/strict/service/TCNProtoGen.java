package org.itoapp.strict.service;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import cafe.cryptography.ed25519.Ed25519PrivateKey;

public class TCNProtoGen {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String SHA256 = "SHA-256";
    private static final byte[] H_TCK = "H_TCK".getBytes(); // Pin charset?
    private static final byte[] H_TCN = "H_TCN".getBytes(); // Pin charset?
    public static final byte TCN_ID_ITO = 0x2;
    public static final byte TCN_ID_COEPI = 0x0;

    byte memotype = TCN_ID_ITO;

    byte[] rak = new byte[32];
    byte[] rvk = new byte[32];

    byte[] startTCK;
    byte[] currentTCK;
    int currentTCKpos = 0;

    public TCNProtoGen() {
        RANDOM.nextBytes(rak);
        genRVKandTck0();
    }

    public TCNProtoGen(byte[] rak, int currentTCKpos) {
        this.rak = rak;
        genRVKandTck0();
        this.currentTCKpos = currentTCKpos;
    }

    public TCNProtoGen(byte memotype) {
        RANDOM.nextBytes(rak);
        genRVKandTck0();
        this.memotype = memotype;
    }

    public TCNProtoGen(byte memotype, byte[] rvk, byte[] startTCK, int startTCKpos) {
        this.rvk = rvk;
        this.startTCK = startTCK;
        currentTCK = startTCK;
        currentTCKpos = startTCKpos;
        this.memotype = memotype;
    }

    void genRVKandTck0() {
        Ed25519PrivateKey sk = Ed25519PrivateKey.fromByteArray(rak);
        rvk = sk.derivePublic().toByteArray();
        MessageDigest h_tck0 = getSHA256();
        h_tck0.update(H_TCK);
        h_tck0.update(rak); // why do we use this ???
        startTCK = h_tck0.digest();
        currentTCK = startTCK;
        currentTCKpos = 0;
    }

    public synchronized byte[] getNewTCN() {
        currentTCK = genNextTCK(currentTCK);
        currentTCKpos++;
        return getCurrentTCN();
    }

    byte[] getCurrentTCN() {
        MessageDigest h_tcnj = getSHA256();
        h_tcnj.update(H_TCN);
        ByteBuffer length = ByteBuffer.allocate(2);
        length.order(ByteOrder.LITTLE_ENDIAN);
        length.putShort((short) (currentTCKpos));
        h_tcnj.update(length.array());
        h_tcnj.update(currentTCK);

        byte[] ret = new byte[16];
        System.arraycopy(h_tcnj.digest(), 0, ret, 0, 16);
        return ret;
    }

    private byte[] genNextTCK(byte[] current) {
        MessageDigest h_tckj = getSHA256();
        h_tckj.update(H_TCK);
        h_tckj.update(rvk);
        h_tckj.update(current);
        return h_tckj.digest();
    }

    public synchronized byte[] generateReport(int previousRatchetTicks) {
        // todo fail if no rak present
        int end = currentTCKpos;
        int start = currentTCKpos - previousRatchetTicks - 1;
        if (currentTCKpos <= 0) { // have we got more than only tck_0?
            throw new RuntimeException("no Keys to report about");
        }
        if (previousRatchetTicks < 0)
            throw new RuntimeException("daysBefore can not be negative");
        if (start < 1) { // give em everything we've got (except tck_0)
            start = 1;
        }
        byte[] memo = createMemo();
        final int totalPayloadbytes = 32 + 32 + 4 + memo.length;

        ByteBuffer payload = ByteBuffer.allocate(totalPayloadbytes);
        payload.put(rvk);
        payload.put(generateTCKAtPosition(start));

        ByteBuffer beginAndEnd = ByteBuffer.allocate(4);
        beginAndEnd.order(ByteOrder.LITTLE_ENDIAN);
        beginAndEnd.putShort((short) (start + 1));
        beginAndEnd.putShort((short) (end + 1));
        payload.put(beginAndEnd.array());

        payload.put(memo);

        byte[] sig = Ed25519PrivateKey.fromByteArray(rak).expand().sign(payload.array(), Ed25519PrivateKey.fromByteArray(rak).derivePublic()).toByteArray();
        ByteBuffer ret = ByteBuffer.allocate(totalPayloadbytes + sig.length);
        ret.put(payload.array());
        ret.put(sig);
        return ret.array();
    }

    private byte[] generateTCKAtPosition(int start) {
        byte[] tmp = startTCK;
        for (int i = 0; i < start; i++) {
            tmp = genNextTCK(tmp);
        }
        return tmp;
    }

    public int getRatchetTickCount() {
        return currentTCKpos;
    }

    private byte[] createMemo() {
        byte[] symptomData = "symptom data".getBytes();
        ByteBuffer memo = ByteBuffer.allocate(2 + symptomData.length);
        memo.order(ByteOrder.LITTLE_ENDIAN);
        memo.put((byte) this.memotype); // 0x2: ITO symptom report v1;
        memo.put((byte) symptomData.length);
        memo.put(symptomData);
        return memo.array();
    }


    private MessageDigest getSHA256() {
        try {
            return MessageDigest.getInstance(SHA256);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

}
