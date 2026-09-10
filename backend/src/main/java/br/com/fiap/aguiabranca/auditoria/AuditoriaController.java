package br.com.fiap.aguiabranca.auditoria;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.aguiabranca.auditoria.dto.AuditoriaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auditoria")
@RequiredArgsConstructor
@Tag(name = "Auditoria")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    @GetMapping
    @Operation(summary = "Lista eventos de auditoria — exclusivo do LIDER")
    @ApiResponse(responseCode = "403", description = "Role insuficiente")
    public ResponseEntity<Page<AuditoriaResponse>> listar(
            @RequestParam(required = false) String ator,
            @RequestParam(required = false) Acao acao,
            @RequestParam(required = false) String recurso,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant ate,
            @PageableDefault(size = 20, sort = "em", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        Page<AuditoriaResponse> pagina = auditoriaService.listar(ator, acao, recurso, de, ate, pageable)
                .map(AuditoriaResponse::de);
        return ResponseEntity.ok(pagina);
    }
}
