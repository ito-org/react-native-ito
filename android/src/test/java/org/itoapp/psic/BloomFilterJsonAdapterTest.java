package org.itoapp.psic;

import static org.junit.Assert.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;

public class BloomFilterJsonAdapterTest {

    @Test
    public void testBloomFilterTypeAdapter() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(BloomFilter.class, new BloomFilterJsonAdapter())
                .registerTypeAdapter(byte[].class, new Base64JsonAdapter())
                .create();

        BloomFilter bloomFilter = new BloomFilter(0.01, 100);
        for (int i = 0; i < 100; i++) {
            bloomFilter.add(("Element " + i).getBytes());
        }

        String encodedFilter = gson.toJson(bloomFilter);
        String expected =
                "{\"num_hash_functions\":7,\"bits\":\"VN3/BXfUjEDvJLcxCTepUCTXGQwlTax" +
                        "0xHiMohCNb45uShFsznK099RH0CFVIMn91Bdc7jLkXHXrXp1NimmZSDrYSj5" +
                        "sd/500nroNOdXbtd53u8cejPMGxbx7kR1E1zyO19mSkYLXq4xf7au5dFN0qh" +
                        "xqfLnjaCE\"}";

        assertEquals(expected, encodedFilter);

        BloomFilter decodedBloomFilter = gson.fromJson(expected, BloomFilter.class);

        assertArrayEquals(bloomFilter.getBackingBytes(), decodedBloomFilter.getBackingBytes());
        assertEquals(bloomFilter.getNumberOfHashFunctions(), decodedBloomFilter.getNumberOfHashFunctions());
    }
}
