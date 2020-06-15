package org.itoapp.psic;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.pqc.math.linearalgebra.IntegerFunctions;
import org.bouncycastle.util.BigIntegers;

import java.math.BigInteger;

/**
 * This is a port of google's private-join-and-compute's commutative cipher'.
 * It uses bouncycastle for the crypto and ec functions.
 * <p>
 * Link to the private-join-and-compute repository:
 * https://github.com/google/private-join-and-compute
 * Most of the comments are copied from the C++ implementation.
 * <p>
 * For now, this class is hardcoded to use the prime256 curve and sha256 for the random oracle.
 * <p>
 * Author (of the port): Christian Darius Romberg (CD-Rom) <distjubo@gmail.com>
 */
public class ECCommutativeCipher {
    private final BigInteger privateKey;
    private final BigInteger inversePrivateKey;
    private final ECCurve curve;
    private final BigInteger curveP;
    private final BigInteger curveA;
    private final BigInteger curveB;

    public ECCommutativeCipher(byte[] privateKey) {
        this(BigIntegers.fromUnsignedByteArray(privateKey));
    }

    public ECCommutativeCipher(BigInteger privateKey) {
        String algorithm = "prime256v1";
        ECNamedCurveParameterSpec parameterSpec = ECNamedCurveTable.getParameterSpec(algorithm);

        // store the private key and inverse private key
        this.privateKey = privateKey.mod(parameterSpec.getN());
        this.inversePrivateKey = privateKey.modInverse(parameterSpec.getN());

        // get and store all the curve parameters
        curve = parameterSpec.getCurve();
        curveA = curve.getA().toBigInteger();
        curveB = curve.getB().toBigInteger();
        curveP = curve.getField().getCharacteristic();
        if ((!curveP.isProbablePrime(20))) throw new AssertionError();
    }

    /**
     * Hashes and encrypts arbitrary plaintext
     */
    public byte[] hashEncrypt(byte[] plaintext) {
        ECPoint hashed_point = hashToTheCurve(plaintext);
        return encrypt(hashed_point);
    }

    /**
     * Encrypts a point that coincides with the curve (such as the point returned from hashEncrypt)
     */
    public byte[] encryptEncodedPoint(byte[] encodedPoint) {
        ECPoint decodedPoint = curve.decodePoint(encodedPoint);
        return encrypt(decodedPoint);
    }

    private byte[] encrypt(ECPoint ecPoint) {
        ECPoint multiplied = ecPoint.multiply(privateKey);
        return multiplied.getEncoded(true);
    }

    /**
     * Decodes and decrypts a point
     */
    public byte[] decrypt(byte[] ciphertext) {
        ECPoint encrypted_point = curve.decodePoint(ciphertext);
        ECPoint decrypted_point = encrypted_point.multiply(inversePrivateKey);
        return decrypted_point.getEncoded(true);
    }

    /**
     * Hashes a string to a point on the elliptic curve using the
     * "try-and-increment" method.
     * See Section 5.2 of https://crypto.stanford.edu/~dabo/papers/bfibe.pdf.
     */
    private ECPoint hashToTheCurve(byte[] plaintext) {
        BigInteger x = randomOracleSha256(plaintext, curveP);
        while (true) {
            ECPoint point = getPointByHashingToCurveInternal(x);
            if (point != null)
                return point;
            x = randomOracleSha256(x.toByteArray(), curveP);
        }
    }

    /**
     * Hashes m to a point on the elliptic curve y^2 = x^3 + ax + b over a
     * prime field using SHA256 with "try-and-increment" method.
     * See https://crypto.stanford.edu/~dabo/papers/bfibe.pdf, Section 5.2.
     * Returns null if there is no valid solution for y for the specified value of x.
     * <p>
     * Security: The number of operations required to hash a string depends on the
     * string, which could lead to a timing attack.
     * Security: This function is only secure for curves of prime order.
     */
    private ECPoint getPointByHashingToCurveInternal(BigInteger x) {
        BigInteger mod_x = x.mod(curveP);
        BigInteger y2 = computeYSquare(mod_x);
        if (IsSquare(y2)) {
            BigInteger sqrt = IntegerFunctions.ressol(y2, curveP);
            if (sqrt.testBit(0)) {
                return curve.createPoint(mod_x, curveP.subtract(sqrt.mod(curveP)));
            }
            return curve.createPoint(mod_x, sqrt);
        }
        return null;
    }

    /**
     * Returns true if q is a quadratic residue modulo p.
     */
    private boolean IsSquare(BigInteger q) {
        return q.modPow(curveP.subtract(BigInteger.ONE).divide(BigInteger.valueOf(2)), curveP).equals(BigInteger.ONE);
    }

    /**
     * Returns y^2 for the given x. The returned value is computed as x^3 + ax + b
     * mod p, where a and b are the parameters of the curve.
     */
    private BigInteger computeYSquare(BigInteger x) {
        return x.pow(3).add(curveA.multiply(x)).add(curveB).mod(curveP);
    }

    /**
     * A random oracle function mapping x deterministically into a large domain.
     * <p>
     * The random oracle is similar to the example given in the last paragraph of
     * Chapter 6 of [1] where the output is expanded by successively hashing the
     * concatenation of the input with a fixed sized counter starting from 1.
     * <p>
     * [1] Bellare, Mihir, and Phillip Rogaway. "Random oracles are practical:
     * A paradigm for designing efficient protocols." Proceedings of the 1st ACM
     * conference on Computer and communications security. ACM, 1993.
     * <p>
     * Returns a long value from the set [0, max_value).
     * <p>
     * Check Error: if bit length of max_value is greater than 130048.
     * Since the counter used for expanding the output is expanded to 8 bit length
     * (hard-coded), any counter value that is greater than 256 would cause
     * variable length inputs passed to the underlying sha256 calls and
     * might make this random oracle's output not uniform across the output
     * domain.
     * <p>
     * The output length is increased by a security value of 256 which reduces
     * the bias of selecting certain values more often than others when max_value
     * is not a multiple of 2.
     */
    private BigInteger randomOracleSha256(byte[] x, BigInteger maxValue) {
        int hash_output_length = 256;
        int output_bit_length = maxValue.bitLength() + hash_output_length;
        int iter_count = (int) Math.ceil((output_bit_length) / (float) hash_output_length);

        if (iter_count * hash_output_length >= 130048)
            throw new IllegalArgumentException(
                    "The domain bit length must not be greater than " +
                            "130048. Desired bit length: " + output_bit_length);

        int excess_bit_count = (iter_count * hash_output_length) - output_bit_length;

        BigInteger hash_output = BigInteger.ZERO;

        SHA256Digest digest = new SHA256Digest();
        byte[] hashed_string = new byte[32];

        for (int i = 1; i < iter_count + 1; i++) {
            hash_output = hash_output.shiftLeft(hash_output_length);

            // compute the hash sha256(i|x)
            digest.update((byte) i);
            digest.update(x, 0, x.length);
            digest.doFinal(hashed_string, 0);

            // add the computed hash to the output
            // use an unsigned BigInteger to match the c++ implementation
            hash_output = hash_output.add(BigIntegers.fromUnsignedByteArray(hashed_string));
        }
        return hash_output.shiftRight(excess_bit_count).mod(maxValue);
    }
}
