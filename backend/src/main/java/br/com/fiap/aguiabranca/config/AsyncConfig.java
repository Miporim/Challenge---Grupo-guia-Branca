package br.com.fiap.aguiabranca.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/** Necessário para o {@code @Async} do listener de auditoria (seção 6). */
@Configuration
@EnableAsync
public class AsyncConfig {
}
