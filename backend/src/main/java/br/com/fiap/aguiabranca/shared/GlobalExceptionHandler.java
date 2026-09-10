package br.com.fiap.aguiabranca.shared;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

/**
 * Handler global de erros. Todas as respostas de erro seguem RFC 7807
 * ({@code application/problem+json}) — ver seção 1 da especificação.
 *
 * As excecões de {@code ia} (502/503) têm seu próprio
 * {@code @RestControllerAdvice} em {@code ia.IaExceptionHandler} — não
 * importadas aqui para não inverter a dependência de pacote (seção 1
 * lista {@code shared} como fundação, sem depender de pacotes de
 * domínio).
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> tratarValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new HashMap<>();
        for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
            campos.put(erro.getField(), erro.getDefaultMessage());
        }
        ProblemDetail problema = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problema.setTitle("Erro de validação");
        problema.setDetail("Um ou mais campos são inválidos");
        problema.setProperty("campos", campos);
        log.warn("Validação falhou: {}", campos);
        return responder(HttpStatus.BAD_REQUEST, problema);
    }

    @ExceptionHandler(RequisicaoInvalidaException.class)
    public ResponseEntity<ProblemDetail> tratarRequisicaoInvalida(RequisicaoInvalidaException ex) {
        return construirEResponder(HttpStatus.BAD_REQUEST, "Requisição inválida", ex);
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ProblemDetail> tratarCredenciaisInvalidas(CredenciaisInvalidasException ex) {
        return construirEResponder(HttpStatus.UNAUTHORIZED, "Não autenticado", ex);
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ProblemDetail> tratarNaoEncontrado(RecursoNaoEncontradoException ex) {
        return construirEResponder(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex);
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ProblemDetail> tratarConflito(ConflitoException ex) {
        return construirEResponder(HttpStatus.CONFLICT, "Conflito", ex);
    }

    @ExceptionHandler(RateLimitExcedidoException.class)
    public ResponseEntity<ProblemDetail> tratarRateLimit(RateLimitExcedidoException ex) {
        return construirEResponder(HttpStatus.TOO_MANY_REQUESTS, "Muitas tentativas", ex);
    }

    private ResponseEntity<ProblemDetail> construirEResponder(HttpStatus status, String titulo, Exception ex) {
        ProblemDetail problema = ProblemDetail.forStatus(status);
        problema.setTitle(titulo);
        problema.setDetail(ex.getMessage());
        if (status.is5xxServerError() || status == HttpStatus.TOO_MANY_REQUESTS) {
            log.warn("{}: {}", titulo, ex.getMessage());
        }
        return responder(status, problema);
    }

    private ResponseEntity<ProblemDetail> responder(HttpStatus status, ProblemDetail problema) {
        problema.setProperty("timestamp", Instant.now());
        problema.setType(URI.create("about:blank"));
        return ResponseEntity.status(status)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                .body(problema);
    }
}
