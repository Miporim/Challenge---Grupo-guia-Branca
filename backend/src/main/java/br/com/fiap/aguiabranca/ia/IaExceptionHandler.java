package br.com.fiap.aguiabranca.ia;

import java.net.URI;
import java.time.Instant;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

/**
 * {@code @RestControllerAdvice} próprio para os erros de IA (502/503) —
 * separado do {@code shared.GlobalExceptionHandler} para não fazer
 * {@code shared} depender de {@code ia} (seção 1: {@code shared} é
 * fundação). Mesmo formato RFC 7807 dos demais erros.
 */
@RestControllerAdvice
@Slf4j
public class IaExceptionHandler {

    @ExceptionHandler(IaIndisponivelException.class)
    public ResponseEntity<ProblemDetail> tratarIaIndisponivel(IaIndisponivelException ex) {
        log.warn("IA indisponível: {}", ex.getMessage());
        return responder(HttpStatus.SERVICE_UNAVAILABLE, "IA indisponível", ex.getMessage());
    }

    @ExceptionHandler(IaRespostaInvalidaException.class)
    public ResponseEntity<ProblemDetail> tratarIaRespostaInvalida(IaRespostaInvalidaException ex) {
        log.warn("Resposta inválida da IA: {}", ex.getMessage());
        return responder(HttpStatus.BAD_GATEWAY, "Resposta inválida da IA", ex.getMessage());
    }

    private ResponseEntity<ProblemDetail> responder(HttpStatus status, String titulo, String detalhe) {
        ProblemDetail problema = ProblemDetail.forStatus(status);
        problema.setTitle(titulo);
        problema.setDetail(detalhe);
        problema.setProperty("timestamp", Instant.now());
        problema.setType(URI.create("about:blank"));
        return ResponseEntity.status(status)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                .body(problema);
    }
}
