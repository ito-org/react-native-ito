package org.itoapp.psic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonSingleton {

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(byte[].class, new Base64JsonAdapter())
            .registerTypeAdapter(BloomFilter.class, new BloomFilterJsonAdapter())
            .create();
}
