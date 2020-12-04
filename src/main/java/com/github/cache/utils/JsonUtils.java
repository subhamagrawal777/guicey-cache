package com.github.cache.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonUtils {

    private static final String JSON_ERROR_MESSAGE = "JSON_ERROR";
    private static ObjectMapper mapper;

    public static void setup(ObjectMapper objectMapper) {
        mapper = objectMapper;
    }


    @Nullable
    static byte[] serialize(Object data) {
        try {
            if (data == null) {
                return null;
            }
            return mapper.writeValueAsBytes(data);
        } catch (JsonProcessingException e) {
            log.error(JSON_ERROR_MESSAGE, e);
            throw AppException.propagate(e, ErrorCode.JSON_ERROR);
        }
    }

    @Nullable
    static <T> T deserialize(byte[] data, Class<T> valueType) {
        try {
            if (data == null) {
                return null;
            }
            return mapper.readValue(data, valueType);
        } catch (Exception e) {
            log.error(JSON_ERROR_MESSAGE, e);
            throw AppException.propagate(e, ErrorCode.JSON_ERROR);
        }
    }

}
