package ru.sber.transport.messaging.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
public class EnvironmentConfig implements EnvironmentPostProcessor, ImportBeanDefinitionRegistrar {

    private static final List<String> OUTPUT_BINDINGS = new LinkedList<>();

    @Override
    public void postProcessEnvironment(@NonNull ConfigurableEnvironment environment, @NonNull SpringApplication application) {
        log.info("Configuring function definitions");
        final var functionalBindings = createFunctionalBindings(environment);
        final var inputBindings = new ArrayList<String>();

        for (final var binding : functionalBindings) {
            if (binding.contains("-in-")) {
                inputBindings.add(binding);
            } else if (binding.contains("-out-")) {
                OUTPUT_BINDINGS.add(binding);
            }
        }

        log.info("Found: {} inputs, {} outputs", inputBindings.size(), OUTPUT_BINDINGS.size());

        final var functionsSource = new HashMap<String, Object>(Map.of(
                "spring.cloud.function.definition", inputBindings.parallelStream().map(it -> it.replaceAll("-in-\\d+", "")).collect(Collectors.joining(";"))
        ));

        environment.getPropertySources().addFirst(new MapPropertySource("Kafka functional bindings", functionsSource));
        log.info("Function definitions configured");
    }

    private @NonNull Collection<String> createFunctionalBindings(@NonNull ConfigurableEnvironment environment) {
        return environment.getPropertySources()
                .stream()
                .parallel()
                .filter(MapPropertySource.class::isInstance)
                .map(MapPropertySource.class::cast)
                .map(MapPropertySource::getPropertyNames)
                .flatMap(Arrays::stream)
                .filter(name -> name.startsWith("spring.cloud.stream.bindings.") && name.endsWith(".destination"))
                .map(name -> name.replace("spring.cloud.stream.bindings.", "").replaceAll("\\.\\w+", ""))
                .toList();
    }

    @Override
    public void registerBeanDefinitions(@NonNull AnnotationMetadata importingClassMetadata, @NonNull BeanDefinitionRegistry registry) {
        log.info("Configuring {} output bindings", OUTPUT_BINDINGS.size());
        if (registry instanceof ConfigurableListableBeanFactory factory) {
            OUTPUT_BINDINGS.parallelStream()
                    .map(it -> new AbstractMap.SimpleEntry<>(it.replaceAll("-out-\\d+", ""), it))
                    .filter(it -> !registry.isBeanNameInUse(it.getKey()))
                    .map(it -> new AbstractMap.SimpleEntry<>(it.getKey(), (Supplier<OutputBridge>) () -> {
                        var streamBridge = factory.getBean(StreamBridge.class);
                        return new OutputBridge(streamBridge) {

                            @Override
                            public String channelId() {
                                return it.getValue();
                            }
                        };
                    }))
                    .map(this::createBeanDefinition)
                    .forEach(it -> registry.registerBeanDefinition(it.getKey(), it.getValue()));
            log.info("{} output bindings configured", OUTPUT_BINDINGS.size());
        } else {
            log.warn("Cannot register output bindings");
        }
    }

    private @NonNull Map.Entry<String, BeanDefinition> createBeanDefinition(@NonNull Map.Entry<String, Supplier<OutputBridge>> binding) {
        var beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(OutputBridge.class);
        beanDefinition.setInstanceSupplier(() -> binding.getValue().get());
        return new AbstractMap.SimpleEntry<>(binding.getKey(), beanDefinition);
    }
}
