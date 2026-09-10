package br.com.fiap.aguiabranca.auditoria;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Único ponto que grava em {@code auditoria} — persiste fora da thread da requisição. */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditoriaListener {

    private final AuditoriaRepository auditoriaRepository;

    @Async
    @EventListener
    public void aoReceberEvento(AuditEvent evento) {
        AuditoriaEvento documento = AuditoriaEvento.builder()
                .ator(evento.ator())
                .acao(evento.acao())
                .recurso(evento.recurso())
                .recursoId(evento.recursoId())
                .alteracoes(Alteracoes.builder().antes(evento.antes()).depois(evento.depois()).build())
                .ip(evento.ip())
                .correlationId(evento.correlationId())
                .em(evento.em())
                .build();

        try {
            auditoriaRepository.save(documento);
        } catch (Exception ex) {
            // Auditoria nunca pode derrubar o fluxo principal — já rodou
            // async e desacoplado do request original.
            log.error("Falha ao persistir evento de auditoria: acao={}, recurso={}", evento.acao(), evento.recurso(), ex);
        }
    }
}
