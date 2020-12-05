package com.github.cache.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.io.IOException;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonUtils {

    private static ObjectMapper mapper;

    public static void setup(ObjectMapper objectMapper) {
        mapper = objectMapper;
    }


    @Nullable
    static byte[] serialize(Object data) throws JsonProcessingException {
        if (data == null) {
            return null;
        }
        return mapper.writeValueAsBytes(data);
    }

    @Nullable
    static <T> T deserialize(String data, Class<T> valueType) throws IOException {
        if (data == null) {
            return null;
        }
        return mapper.readValue(data, valueType);
    }

    @Nullable
    static <T> T deserialize(String data, JavaType javaType) throws IOException {
        if (data == null) {
            return null;
        }
        return mapper.readValue(data, javaType);
    }

}
