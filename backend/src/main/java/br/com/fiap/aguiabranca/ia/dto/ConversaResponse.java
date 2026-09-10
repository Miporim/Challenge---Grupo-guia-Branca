package br.com.fiap.aguiabranca.ia.dto;

import java.time.Instant;
import java.util.List;

import br.com.fiap.aguiabranca.ia.ConversaIa;

public record ConversaResponse(
        String id,
        String titulo,
        List<MensagemResponse> mensagens,
        Instant criadoEm,
        Instant atualizadoEm
) {
    public static ConversaResponse de(ConversaIa conversa) {
        List<MensagemResponse> mensagens = conversa.getMensagens() == null
                ? List.of()
                : conversa.getMensagens().stream().map(MensagemResponse::de).toList();
        return new ConversaResponse(
                conversa.getId(), conversa.getTitulo(), mensagens, conversa.getCriadoEm(), conversa.getAtualizadoEm());
    }
}
