package com.github.cache.utils;

import com.fasterxml.jackson.databind.JavaType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CompressionUtils {

    public static <T> byte[] encode(T data) throws IOException {
        if (data == null) {
            return null;
        }
        val byteArrayOutputStream = new ByteArrayOutputStream();
        val gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
        gzipOutputStream.write(JsonUtils.serialize(data));
        gzipOutputStream.flush();
        gzipOutputStream.close();
        return Base64.getEncoder().encode(byteArrayOutputStream.toByteArray());

    }

    public static <T> T decode(byte[] data, Class<T> tClass) throws IOException {
        return JsonUtils.deserialize(decode(data), tClass);
    }

    public static <T> T decode(byte[] data, JavaType javaType) throws IOException {
        return JsonUtils.deserialize(decode(data), javaType);
    }

    private static String decode(byte[] data) throws IOException {
        val byteArrayInputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
        val gzipInputStream = new GZIPInputStream(byteArrayInputStream);
        val inputStreamReader = new InputStreamReader(gzipInputStream, StandardCharsets.UTF_8);
        val bufferedReader = new BufferedReader(inputStreamReader);
        val output = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            output.append(line);
        }
        return output.toString();
    }

}
