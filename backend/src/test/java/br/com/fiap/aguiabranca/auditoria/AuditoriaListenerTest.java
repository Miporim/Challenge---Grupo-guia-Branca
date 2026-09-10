package br.com.fiap.aguiabranca.auditoria;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AuditoriaListenerTest {

    private final AuditoriaRepository auditoriaRepository = mock(AuditoriaRepository.class);
    private final AuditoriaListener listener = new AuditoriaListener(auditoriaRepository);

    @Test
    void persisteEventoComTodosOsCampos() {
        Ator ator = Ator.builder().userId("usr-1").email("a@a.com").role("LIDER").build();
        AuditEvent evento = new AuditEvent(ator, Acao.CRIAR, "estrategia", "id-1",
                null, Map.of("titulo", "T"), "127.0.0.1", "cid-1", Instant.now());

        listener.aoReceberEvento(evento);

        ArgumentCaptor<AuditoriaEvento> captor = ArgumentCaptor.forClass(AuditoriaEvento.class);
        verify(auditoriaRepository).save(captor.capture());
        AuditoriaEvento salvo = captor.getValue();

        assertEquals("usr-1", salvo.getAtor().getUserId());
        assertEquals(Acao.CRIAR, salvo.getAcao());
        assertEquals("estrategia", salvo.getRecurso());
        assertEquals("id-1", salvo.getRecursoId());
        assertEquals(Map.of("titulo", "T"), salvo.getAlteracoes().getDepois());
        assertEquals("127.0.0.1", salvo.getIp());
        assertEquals("cid-1", salvo.getCorrelationId());
    }

    @Test
    void falhaAoPersistirNaoPropagaExcecao() {
        AuditEvent evento = new AuditEvent(
                Ator.builder().build(), Acao.LOGIN, "usuario", null, null, null, null, null, Instant.now());
        doThrow(new RuntimeException("Mongo indisponível")).when(auditoriaRepository).save(any());

        assertDoesNotThrow(() -> listener.aoReceberEvento(evento));
    }
}
