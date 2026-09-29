package ru.sber.transport.grpc.common.marshallers;


import io.grpc.Metadata;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Маршаллер строк.
 */
public class StringMarshaller implements Metadata.AsciiMarshaller<String> {
    
    @Override
    public String toAsciiString(String s) {
        var encoded = Base64.getEncoder().encode(s.getBytes());
        return new String(encoded, StandardCharsets.US_ASCII);
    }
    
    @Override
    public String parseAsciiString(String s) {
        var decoded = Base64.getDecoder().decode(s.getBytes(StandardCharsets.US_ASCII));
        return new String(decoded);
    }
}
