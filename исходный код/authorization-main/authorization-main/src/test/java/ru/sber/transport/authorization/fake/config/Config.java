package ru.sber.transport.authorization.fake.config;

import org.springframework.context.annotation.Configuration;
import ru.sber.transport.authorization.annotations.SkipConsentCheck;

@SkipConsentCheck("skipUrlInConfig")
@Configuration
public class Config {
}
