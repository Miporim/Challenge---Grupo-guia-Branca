package br.com.fiap.aguiabranca.estrategia;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.aguiabranca.estrategia.dto.EstrategiaRequest;
import br.com.fiap.aguiabranca.estrategia.dto.EstrategiaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * A verificação de role (LIDER) é feita em {@code EstrategiaService} via
 * {@code @PreAuthorize}; leitura é liberada para qualquer autenticado
 * (regra de rota em {@code SecurityConfig}).
 */
@RestController
@RequestMapping("/api/estrategias")
@RequiredArgsConstructor
@Tag(name = "Estratégias")
public class EstrategiaController {

    private final EstrategiaService estrategiaService;

    @PostMapping
    @Operation(summary = "Cria uma estratégia — exclusivo do LIDER")
    @ApiResponse(responseCode = "403", description = "Role insuficiente")
    public ResponseEntity<EstrategiaResponse> criar(@Valid @RequestBody EstrategiaRequest request) {
        Estrategia estrategia = estrategiaService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(EstrategiaResponse.de(estrategia));
    }

    @GetMapping
    @Operation(summary = "Lista estratégias com filtros opcionais")
    public ResponseEntity<Page<EstrategiaResponse>> listar(
            @RequestParam(required = false) EstrategiaStatus status,
            @RequestParam(required = false) Categoria categoria,
            @RequestParam(required = false) String campanha,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<EstrategiaResponse> pagina = estrategiaService.listar(status, categoria, campanha, pageable)
                .map(EstrategiaResponse::de);
        return ResponseEntity.ok(pagina);
    }

    @GetMapping("/vigente")
    @Operation(summary = "Estratégia vigente mais recente")
    @ApiResponse(responseCode = "404", description = "Nenhuma estratégia vigente")
    public ResponseEntity<EstrategiaResponse> vigente() {
        return ResponseEntity.ok(EstrategiaResponse.de(estrategiaService.vigenteAtual()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhe de uma estratégia")
    @ApiResponse(responseCode = "404", description = "Estratégia não encontrada")
    public ResponseEntity<EstrategiaResponse> buscarPorId(@PathVariable String id) {
        return ResponseEntity.ok(EstrategiaResponse.de(estrategiaService.buscarPorId(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma estratégia — exclusivo do LIDER")
    @ApiResponse(responseCode = "403", description = "Role insuficiente")
    @ApiResponse(responseCode = "404", description = "Estratégia não encontrada")
    public ResponseEntity<EstrategiaResponse> atualizar(
            @PathVariable String id,
            @Valid @RequestBody EstrategiaRequest request) {
        return ResponseEntity.ok(EstrategiaResponse.de(estrategiaService.atualizar(id, request)));
    }

    @PatchMapping("/{id}/encerrar")
    @Operation(summary = "Encerra uma estratégia — exclusivo do LIDER")
    @ApiResponse(responseCode = "403", description = "Role insuficiente")
    @ApiResponse(responseCode = "404", description = "Estratégia não encontrada")
    public ResponseEntity<EstrategiaResponse> encerrar(@PathVariable String id) {
        return ResponseEntity.ok(EstrategiaResponse.de(estrategiaService.encerrar(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma estratégia — exclusivo do LIDER")
    @ApiResponse(responseCode = "403", description = "Role insuficiente")
    @ApiResponse(responseCode = "404", description = "Estratégia não encontrada")
    @ApiResponse(responseCode = "409", description = "Há ideias ou projetos vinculados")
    public ResponseEntity<Void> excluir(@PathVariable String id) {
        estrategiaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
