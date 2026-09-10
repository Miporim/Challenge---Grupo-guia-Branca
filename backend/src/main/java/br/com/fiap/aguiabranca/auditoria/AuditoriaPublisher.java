package br.com.fiap.aguiabranca.auditoria;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import br.com.fiap.aguiabranca.security.SecurityUtils;
import lombok.RequiredArgsConstructor;

/**
 * Ponto único de publicação de {@link AuditEvent} — os services chamam
 * este bean, nunca o {@code ApplicationEventPublisher} diretamente, para
 * padronizar como {@code ator}/{@code ip}/{@code correlationId} são
 * resolvidos (sempre na thread da requisição, antes do listener
 * {@code @Async}).
 */
@Component
@RequiredArgsConstructor
public class AuditoriaPublisher {

    private final ApplicationEventPublisher eventPublisher;

    /** Uso comum: o ator é o usuário autenticado (lido do SecurityContext). */
    public void publicar(Acao acao, String recurso, String recursoId, Map<String, Object> antes, Map<String, Object> depois) {
        Ator ator = Ator.builder()
                .userId(SecurityUtils.getUsuarioId())
                .email(SecurityUtils.getEmail())
                .role(SecurityUtils.getRole() != null ? SecurityUtils.getRole().name() : null)
                .build();
        publicarComAtor(ator, acao, recurso, recursoId, antes, depois);
    }

    /** Uso para LOGIN/LOGIN_FALHA — ainda não há usuário autenticado no contexto. */
    public void publicarComAtor(Ator ator, Acao acao, String recurso, String recursoId,
            Map<String, Object> antes, Map<String, Object> depois) {
        AuditEvent evento = new AuditEvent(
                ator, acao, recurso, recursoId, antes, depois,
                obterIp(), MDC.get("correlationId"), Instant.now());
        eventPublisher.publishEvent(evento);
    }

    private String obterIp() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs)) {
            return null;
        }
        return attrs.getRequest().getRemoteAddr();
    }

    /**
     * Monta um mapa {@code chave, valor, chave, valor, ...} para
     * {@code alteracoes.antes}/{@code depois} — ao contrário de
     * {@code Map.of}, aceita valor {@code null} (um campo pode
     * legitimamente não ter valor, e {@code Map.of} lançaria
     * {@code NullPointerException}).
     */
    public static Map<String, Object> mapa(Object... paresChaveValor) {
        Map<String, Object> mapa = new HashMap<>();
        for (int i = 0; i < paresChaveValor.length; i += 2) {
            mapa.put((String) paresChaveValor[i], paresChaveValor[i + 1]);
        }
        return mapa;
    }
}
