package ru.sberbank.ditsib.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.io.File;
import java.io.FileInputStream;
import java.util.Optional;
import java.util.Properties;

/**
 * Загрузчик дополнительных настроек.
 */
@RequiredArgsConstructor
@Configuration
public class AdditionalPropertiesLoader implements InitializingBean {
    
    private final ConfigurableEnvironment environment;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        var resource = getClass().getClassLoader().getResource(".");
        if (resource == null) {
            return;
        }
        var root = resource.getFile();
        var directory = new File(root);
        if (directory.isDirectory()) {
            var files = Optional.ofNullable(directory.listFiles()).orElse(new File[0]);
            for (var file : files) {
                if (!file.getName().startsWith("application") && file.getName().endsWith(".properties")) {
                    try (var is = new FileInputStream(file)) {
                        var properties = new Properties();
                        properties.load(is);
                        var name = String.format("[additionalProperties: %s]", file.getName());
                        environment.getPropertySources().addFirst(new PropertiesPropertySource(name, properties));
                    }
                }
            }
        }
    }
}
