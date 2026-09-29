package ru.sberbank.ditsib.corpclient.service.import_easup.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
class EasupImportConfig {

    @Bean("easupExecutorService")
    ExecutorService easupExecutorService(@Value("${easup.executor.parallelism:6}") int threadsCount) {
        return Executors.newFixedThreadPool(threadsCount);
    }
}
