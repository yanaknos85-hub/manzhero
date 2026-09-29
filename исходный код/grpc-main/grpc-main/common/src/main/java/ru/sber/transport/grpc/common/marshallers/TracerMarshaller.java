package ru.sber.transport.grpc.common.marshallers;

import io.grpc.Metadata;
import io.micrometer.tracing.TraceContext;

/**
 * Маршаллер данных аутентификации.
 */
public class TracerMarshaller implements Metadata.AsciiMarshaller<TraceContext> {

    @Override
    public String toAsciiString(TraceContext traceContext) {
        return "%s|%s|%s".formatted(traceContext.parentId(), traceContext.traceId(), traceContext.spanId());
    }

    @Override
    public TraceContext parseAsciiString(String s) {
        var splitted = s.split("\\|");
        return new TraceContext() {

            @Override
            public String traceId() {
                return splitted[1];
            }

            @Override
            public String parentId() {
                return splitted[0];
            }

            @Override
            public String spanId() {
                return splitted[2];
            }

            @Override
            public Boolean sampled() {
                return false;
            }
        };
    }
}
