package org.itoapp.psic;

import org.bouncycastle.util.encoders.Base64;

import static org.junit.Assert.*;

import org.junit.Test;

public class BloomFilterTest {
    @Test
    public void testBloomFilter() {
        BloomFilter bloomFilter = new BloomFilter(0.1, 1000);

        byte[] element1 = new byte[]{1, 2, 3};
        byte[] element2 = new byte[]{3, 2, 1};
        byte[] element3 = new byte[]{1, 1, 2, 3, 5, 8};

        bloomFilter.add(element1);
        bloomFilter.add(element2);

        assertTrue(bloomFilter.contains(element1));
        assertTrue(bloomFilter.contains(element2));
        assertFalse(bloomFilter.contains(element3));
    }

    @Test
    public void testCompatibilityWithOM() {
        // data taken from here: https://github.com/OpenMined/PSI/blob/master/private_set_intersection/cpp/bloom_filter_test.cpp#L85-L89
        int nHashFunctions = 7;
        byte[] bytes = Base64.decode("VN3/BXfUjEDvJLcxCTepUCTXGQwlTax0xHiMohCNb45uShFsznK099RH0CFVIMn91Bdc7jLkXHXrXp1NimmZSDrYSj5sd/500nroNOdXbtd53u8cejPMGxbx7kR1E1zyO19mSkYLXq4xf7au5dFN0qhxqfLnjaCE");

        BloomFilter bloomFilter = new BloomFilter(nHashFunctions, bytes);

        for (int i = 0; i < 100; i++) {
            assertTrue(bloomFilter.contains(("Element " + 1).getBytes()));
        }
    }
}
