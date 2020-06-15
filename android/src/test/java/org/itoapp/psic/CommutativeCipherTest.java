package org.itoapp.psic;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertArrayEquals;

public class CommutativeCipherTest {

    /**
     * This method tests whether the following is true:
     * a(B(A(h(x))) = B(h(x))
     * <p>
     * where h denotes a deterministic mapping with a random oracle and
     * A denotes encryption with the first key and
     * a denotes decryption with the first key and
     * B denotes encryption with the second key and
     * x denotes random data
     */
    @Test
    public void testCommutativeCipher() {
        byte[] privateKeyA = new byte[32];
        byte[] privateKeyB = new byte[32];
        byte[] originalData = new byte[32];

        // fix the seed for reproducible values
        Random random = new Random();

        // generate the random keys
        random.nextBytes(privateKeyA);
        random.nextBytes(privateKeyB);

        // generate the random input data
        random.nextBytes(originalData);

        // create a cipher with the first key
        ECCommutativeCipher cipherA = new ECCommutativeCipher(privateKeyA);

        // create a cipher with the second key
        ECCommutativeCipher cipherB = new ECCommutativeCipher(privateKeyB);

        // calculate the first part of the equation
        byte[] encryptedData = cipherA.hashEncrypt(originalData);
        byte[] reencryptedData = cipherB.encryptEncodedPoint(encryptedData);
        byte[] decryptedData = cipherA.decrypt(reencryptedData);

        // calculate the second part of the equation
        byte[] independentlyEncryptedData = cipherB.hashEncrypt(originalData);

        // do the test!
        assertArrayEquals(independentlyEncryptedData, decryptedData);
    }
}
