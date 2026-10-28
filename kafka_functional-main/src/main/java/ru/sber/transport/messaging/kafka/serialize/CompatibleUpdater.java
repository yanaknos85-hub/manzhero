package ru.sber.transport.messaging.kafka.serialize;

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
class CompatibleUpdater {

    private static final String SCHEMA_REGISTRY = "schema.registry.";

    private static final String COMPATIBLE = ".compatible";

    private static final String ALL_TOPICS = "global";

    private final Map<String, Modes> compatibles = new HashMap<>(Map.of(ALL_TOPICS, Modes.FORWARD));

    public void update(SchemaRegistryClient schemaRegistry, String topic, String name) throws RestClientException, IOException {
        final var compatibility = compatibles.getOrDefault(topic, compatibles.get(ALL_TOPICS));
        final var localCompatibility = Objects.requireNonNull(compatibility).name();
        final var remoteCompatibility = schemaRegistry.getCompatibility(name);
        if (!Objects.equals(remoteCompatibility.toLowerCase(), localCompatibility.toLowerCase())) {
            schemaRegistry.updateCompatibility(name, localCompatibility);
        }
    }

    public void configure(Map<String, ?> configs) {
        for (final var config : configs.entrySet()) {
            final var key = config.getKey();
            if (key.startsWith(SCHEMA_REGISTRY) && key.endsWith(COMPATIBLE)) {
                final var subject = key.replace(SCHEMA_REGISTRY, "").replace(COMPATIBLE, "");
                final var compatibility = Modes.valueOf(String.valueOf(config.getValue()));
                if (Modes.NONE.equals(compatibility)) {
                    log.warn("For topic {} compatibility mode setted to {}. This will have an impact to compatibility with other versions! Use thos very carefully", subject, compatibility);
                }
                compatibles.put(subject, compatibility);
            }
        }
    }
}
