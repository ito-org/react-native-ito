package org.itoapp.psic;

import com.google.gson.*;
import org.bouncycastle.util.encoders.Base64;

import java.lang.reflect.Type;

class Base64JsonAdapter implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {
    @Override
    public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if(!json.isJsonPrimitive())
        {
            throw new JsonParseException("byte[] has to be a JSON string!");
        }
        return Base64.decode(json.getAsString());
    }

    @Override
    public JsonElement serialize(byte[] data, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(Base64.toBase64String(data));
    }
}
