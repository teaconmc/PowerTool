package org.teacon.powertool.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;

import java.io.StringReader;

public class ParserUtils {
    
    @SuppressWarnings("unchecked")
    public static <T> T parseJson(HolderLookup.Provider registries, String str, Codec<T> codec) {
        JsonReader jsonreader = new JsonReader(new StringReader(str));
        jsonreader.setLenient(false);
        
        Object object;
        try {
            JsonElement jsonelement = Streams.parse(jsonreader);
            object = codec.parse(registries.createSerializationContext(JsonOps.INSTANCE), jsonelement).getOrThrow(JsonParseException::new);
        } catch (StackOverflowError stackoverflowerror) {
            throw new JsonParseException(stackoverflowerror);
        }
        
        return (T) object;
    }
}
