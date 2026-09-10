package br.com.fiap.aguiabranca.auditoria;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class AuditoriaPublisherTest {

    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private final AuditoriaPublisher auditoriaPublisher = new AuditoriaPublisher(eventPublisher);

    @AfterEach
    void limpar() {
        SecurityContextHolder.clearContext();
        MDC.clear();
    }

    @Test
    void publicarResolveAtorDoContextoDeSeguranca() {
        var autenticacao = new UsernamePasswordAuthenticationToken(
                "usr-1", null, List.of(new SimpleGrantedAuthority("ROLE_LIDER")));
        autenticacao.setDetails("lider@aguiabranca.com");
        SecurityContextHolder.getContext().setAuthentication(autenticacao);
        MDC.put("correlationId", "cid-123");

        auditoriaPublisher.publicar(Acao.CRIAR, "estrategia", "id-1", null, Map.of("titulo", "T"));

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        AuditEvent evento = captor.getValue();

        assertEquals("usr-1", evento.ator().getUserId());
        assertEquals("lider@aguiabranca.com", evento.ator().getEmail());
        assertEquals("LIDER", evento.ator().getRole());
        assertEquals(Acao.CRIAR, evento.acao());
        assertEquals("estrategia", evento.recurso());
        assertEquals("id-1", evento.recursoId());
        assertEquals("cid-123", evento.correlationId());
    }

    @Test
    void publicarComAtorUsaAtorExplicito() {
        Ator atorFalha = Ator.builder().userId(null).email("desconhecido@aguiabranca.com").role(null).build();

        auditoriaPublisher.publicarComAtor(atorFalha, Acao.LOGIN_FALHA, "usuario", null, null, null);

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        AuditEvent evento = captor.getValue();

        assertNull(evento.ator().getUserId());
        assertEquals("desconhecido@aguiabranca.com", evento.ator().getEmail());
        assertEquals(Acao.LOGIN_FALHA, evento.acao());
    }
}
