package br.com.fiap.aguiabranca.ia.dto;

import java.time.Instant;

import br.com.fiap.aguiabranca.ia.MensagemIa;

public record MensagemResponse(String papel, String texto, Instant em) {
    public static MensagemResponse de(MensagemIa mensagem) {
        return new MensagemResponse(mensagem.getPapel(), mensagem.getTexto(), mensagem.getEm());
    }
}
