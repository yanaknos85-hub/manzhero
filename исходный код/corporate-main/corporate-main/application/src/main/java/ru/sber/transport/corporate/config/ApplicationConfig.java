package ru.sber.transport.corporate.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import ru.sber.transport.corporate.messaging.senders.DelegateSender;
import ru.sber.transport.corporate.messaging.senders.impl.DelegateSenderImpl;
import ru.sber.transport.corporate.providers.Delegates;
import ru.sber.transport.corporate.proviers.database.DelegatesImpl;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
class ApplicationConfig {

    public static final String DATABASE_EXECUTOR = "databaseExecutor";
    private static final Logger log = LoggerFactory.getLogger(ApplicationConfig.class);

    @Primary
    @Bean("bootstrapExecutor")
    Executor bootstrapExecutor() {
        return Executors.newSingleThreadExecutor();
    }

    @Bean(DATABASE_EXECUTOR)
    ExecutorService databaseExecutor(@Value("${spring.datasource.hikari.maximum-pool-size:10}") int maxPool) {
        return Executors.newFixedThreadPool(maxPool / 2, new ThreadFactoryBuilder().setNameFormat("Database - %d").build());
    }

    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        return factory;
    }

    @Primary
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public Delegates delegatesProvider(DSLContext context) {
        log.info("Configuring delegates provider");
        return new DelegatesImpl() {
            @Override
            public DSLContext context() {
                return context;
            }
        };
    }

    @Bean
    public DelegateSender delegateSender(@Qualifier("delegateOutputAvro") ObjectProvider<OutputBridge> delegateOutputAvro) {
        return new DelegateSenderImpl(delegateOutputAvro);
    }

}
