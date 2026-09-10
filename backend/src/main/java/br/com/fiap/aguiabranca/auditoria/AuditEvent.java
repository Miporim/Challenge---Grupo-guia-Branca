package br.com.fiap.aguiabranca.auditoria;

import java.time.Instant;
import java.util.Map;

/**
 * Publicado via {@code ApplicationEventPublisher} pelos services — nenhum
 * service chama {@link AuditoriaRepository} diretamente (seção 6). Todos
 * os campos já resolvidos no momento da publicação (thread da requisição):
 * o listener é {@code @Async} e roda em outra thread, onde MDC e
 * {@code RequestContextHolder} não estão disponíveis.
 */
public record AuditEvent(
        Ator ator,
        Acao acao,
        String recurso,
        String recursoId,
        Map<String, Object> antes,
        Map<String, Object> depois,
        String ip,
        String correlationId,
        Instant em
) {
}
