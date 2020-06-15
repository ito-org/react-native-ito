package org.itoapp.psic;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;


public class BloomFilterJsonAdapter implements JsonSerializer<BloomFilter>, JsonDeserializer<BloomFilter> {
    public static final String NUM_HASH_FUNCTIONS = "num_hash_functions";
    public static final String BITS = "bits";

    @Override
    public JsonElement serialize(BloomFilter bloomFilter, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(NUM_HASH_FUNCTIONS, bloomFilter.getNumberOfHashFunctions());
        jsonObject.add(BITS, context.serialize(bloomFilter.getBackingBytes()));
        return jsonObject;
    }

    @Override
    public BloomFilter deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!json.isJsonObject())
            throw new JsonParseException("BloomFilter has to be a JSON object!");

        JsonObject jsonObject = (JsonObject) json;

        // Create a copy of the keyset to verify that the necessary keys are present but no extra keys are included
        Set<String> keySet = new HashSet<>(jsonObject.keySet());
        if (!keySet.remove(BITS))
            throw new JsonParseException("Missing bits field!");
        if (!keySet.remove(NUM_HASH_FUNCTIONS))
            throw new JsonParseException("Missing num_hash_functions field!");
        for (String name : keySet)
            throw new JsonParseException("Unexpected field: " + name);

        byte[] bits = context.deserialize(jsonObject.get(BITS), byte[].class);
        int num_hash_functions = jsonObject.get(NUM_HASH_FUNCTIONS).getAsInt();

        if (bits.length < 1)
            throw new JsonParseException("bits has invalid length " + bits.length);

        if (num_hash_functions < 1)
            throw new JsonParseException("num_hash_functions has invalid value " + num_hash_functions);

        return new BloomFilter(num_hash_functions, bits);
    }
}
