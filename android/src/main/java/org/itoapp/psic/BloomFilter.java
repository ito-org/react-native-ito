package org.itoapp.psic;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.util.BigIntegers;

import java.math.BigInteger;

public class BloomFilter {

    /**
     * Returns the number of hash functions used in this bloom filter
     */
    public int getNumberOfHashFunctions() {
        return nHashFunctions;
    }

    /**
     * Returns the backing bytes of this bloom filter. Note that this is not
     * a deep copy; Any changed made to this array will affect the filter.
     */
    public byte[] getBackingBytes() {
        return bytes;
    }

    /**
     * The number of hash functions used in this bloom filter
     */
    private final int nHashFunctions;

    /**
     * The byte array backing this bloom filter
     */
    private final byte[] bytes;


    /**
     * Calculate the number of bits required to archive the given falsePositiveRate with the given number of elements.
     * Formula taken from Wikipedia:
     * https://en.wikipedia.org/wiki/Bloom_filter#Optimal_number_of_hash_functions
     */
    private static long calculateNumberOfBits(double falsePositiveRate, long maxElements) {
        if (falsePositiveRate <= 0 || falsePositiveRate >= 1) {
            throw new IllegalArgumentException("`falsePositiveRate` must be in (0,1)");
        }
        if (maxElements <= 0) {
            throw new IllegalArgumentException("`maxElements` must be positive");
        }

        return (long) Math.ceil((-maxElements * Math.log(falsePositiveRate)) / Math.pow(Math.log(2), 2));
    }

    /**
     * Calculate the number of hash functions required to archive the given falsePositiveRate with the given number of elements.
     * Formula taken from Wikipedia:
     * https://en.wikipedia.org/wiki/Bloom_filter#Optimal_number_of_hash_functions
     */
    private static int calculateNumberOfHashFunctions(double falsePositiveRate, long maxElements) {
        long numBits = calculateNumberOfBits(falsePositiveRate, maxElements);

        return (int) Math.round(numBits / (double) maxElements * Math.log(2));
    }


    /**
     * Creates a new BloomFilter with the given falsePositiveRate and the number of elements expected to be inserted
     */
    public BloomFilter(double falsePositiveRate, long maxElements) {
        this(calculateNumberOfHashFunctions(falsePositiveRate, maxElements),
                (int) Math.ceil(calculateNumberOfBits(falsePositiveRate, maxElements) / 8.0));
    }

    private BloomFilter(int nHashFunctions, int nBytes) {
        this(nHashFunctions, new byte[nBytes]);
    }

    /**
     * Create a bloom filter with the given number of hash functions and an existing backing array
     */
    public BloomFilter(int nHashFunctions, byte[] bytes) {
        if (nHashFunctions < 1) {
            throw new IllegalArgumentException("Number of hash functions must be greater than 0!");
        }
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("bytes must not be null or of zero length!");
        }

        this.nHashFunctions = nHashFunctions;
        this.bytes = bytes;
    }

    /**
     * Insert an element into this filter
     */
    public void add(byte[] element) {
        for (long index : hash(element)) {
            bytes[(int) (index / 8)] |= (1 << (index % 8));
        }
    }

    /**
     * Check whether the specified element is present in this filter. A return value
     * of `true` means that the element might be present while a return value of `false`
     * means that the element is definitely not present in this filter
     */
    public boolean contains(byte[] element) {
        boolean result = true;
        for (long index : hash(element)) {
            result &= ((bytes[(int) (index / 8)] >> (index % 8)) & 1) != 0;
        }
        return result;
    }

    /**
     * Calculates multiple hash values of the given element.
     * These values are calculated such that they can be used as
     * bit-indices for the backing array (modulo the number of bits).
     */
    private long[] hash(byte[] element) {
        // Compute the number of bits
        long numBits = 8L * bytes.length;
        BigInteger numBitsBigInt = BigInteger.valueOf(numBits);

        long[] result = new long[nHashFunctions];

        SHA256Digest digest = new SHA256Digest();
        byte[] hashResult = new byte[32];


        // calculate h1 (SHA256(1 | element))
        digest.update((byte) '1');
        digest.update(element, 0, element.length);
        digest.doFinal(hashResult, 0);

        long h1 = BigIntegers.fromUnsignedByteArray(hashResult).mod(numBitsBigInt).longValue();


        // calculate h2 (SHA256(2 | element))
        digest.update((byte) '2');
        digest.update(element, 0, element.length);
        digest.doFinal(hashResult, 0);

        long h2 = BigIntegers.fromUnsignedByteArray(hashResult).mod(numBitsBigInt).longValue();


        // Compute the i-th hash function as SHA256(1 | element) + i * SHA256(2 | element)
        // (modulo numBits).
        for (int i = 0; i < nHashFunctions; i++) {
            result[i] = (h1 + i * h2) % numBits;
        }
        return result;
    }
}
