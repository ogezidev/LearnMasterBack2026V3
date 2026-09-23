package com.example.learnmaster.tccv2.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

// Permite enviar e-mails em segundo plano (@Async)
@Configuration
@EnableAsync
public class AsyncConfig {
}
