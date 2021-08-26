package org.apache.bookkeeper.bookie;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class FileInfoUtil {

    public static ByteBuffer createBuffer(String content) {
        return ByteBuffer.wrap(content.getBytes(StandardCharsets.UTF_8));
    }
}