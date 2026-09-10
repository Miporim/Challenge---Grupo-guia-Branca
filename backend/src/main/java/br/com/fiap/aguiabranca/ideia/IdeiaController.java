package br.com.fiap.aguiabranca.ideia;

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

import br.com.fiap.aguiabranca.ideia.dto.IdeiaRequest;
import br.com.fiap.aguiabranca.ideia.dto.IdeiaResponse;
import br.com.fiap.aguiabranca.ideia.dto.PriorizacaoRequest;
import br.com.fiap.aguiabranca.ideia.dto.StatusIdeiaRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * A verificação de role e de posse (autor) é feita em
 * {@code IdeiaService} via {@code @PreAuthorize} — este controller só
 * mapeia HTTP.
 */
@RestController
@RequestMapping("/api/ideias")
@RequiredArgsConstructor
@Tag(name = "Ideias")
public class IdeiaController {

    private final IdeiaService ideiaService;

    @PostMapping
    @Operation(summary = "Cria uma ideia — exclusivo do OPERADOR")
    @ApiResponse(responseCode = "403", description = "Role insuficiente")
    @ApiResponse(responseCode = "400", description = "Estratégia inexistente, não vigente ou status inicial inválido")
    public ResponseEntity<IdeiaResponse> criar(@Valid @RequestBody IdeiaRequest request) {
        Ideia ideia = ideiaService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(IdeiaResponse.de(ideia));
    }

    @GetMapping("/minhas")
    @Operation(summary = "Lista as ideias do OPERADOR autenticado")
    public ResponseEntity<Page<IdeiaResponse>> listarMinhas(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ideiaService.listarMinhas(pageable).map(IdeiaResponse::de));
    }

    @GetMapping
    @Operation(summary = "Lista ideias — GESTOR e LIDER")
    @ApiResponse(responseCode = "403", description = "Role insuficiente")
    public ResponseEntity<Page<IdeiaResponse>> listar(
            @RequestParam(required = false) StatusIdeia status,
            @RequestParam(required = false) String estrategiaId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ideiaService.listar(status, estrategiaId, pageable).map(IdeiaResponse::de));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhe de uma ideia — autor, GESTOR ou LIDER")
    @ApiResponse(responseCode = "403", description = "Não é o autor nem GESTOR/LIDER")
    @ApiResponse(responseCode = "404", description = "Ideia não encontrada")
    public ResponseEntity<IdeiaResponse> buscarPorId(@PathVariable String id) {
        return ResponseEntity.ok(IdeiaResponse.de(ideiaService.buscarParaVisualizacao(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma ideia — exclusivo do autor")
    @ApiResponse(responseCode = "403", description = "Não é o autor")
    @ApiResponse(responseCode = "404", description = "Ideia não encontrada")
    @ApiResponse(responseCode = "409", description = "Ideia não está em RASCUNHO ou SUBMETIDA")
    public ResponseEntity<IdeiaResponse> atualizar(@PathVariable String id, @Valid @RequestBody IdeiaRequest request) {
        return ResponseEntity.ok(IdeiaResponse.de(ideiaService.atualizar(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma ideia — autor ou GESTOR")
    @ApiResponse(responseCode = "403", description = "Não é o autor nem GESTOR")
    @ApiResponse(responseCode = "404", description = "Ideia não encontrada")
    @ApiResponse(responseCode = "409", description = "Ideia aprovada não pode ser excluída")
    public ResponseEntity<Void> excluir(@PathVariable String id) {
        ideiaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/priorizacao")
    @Operation(summary = "Prioriza (vota) uma ideia — exclusivo do GESTOR, um voto por gestor")
    @ApiResponse(responseCode = "403", description = "Role insuficiente")
    @ApiResponse(responseCode = "404", description = "Ideia não encontrada")
    @ApiResponse(responseCode = "409", description = "Ideia não está em SUBMETIDA ou EM_ANALISE")
    public ResponseEntity<IdeiaResponse> priorizar(
            @PathVariable String id,
            @Valid @RequestBody PriorizacaoRequest request) {
        return ResponseEntity.ok(IdeiaResponse.de(ideiaService.priorizar(id, request)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Aprova ou reprova uma ideia — exclusivo do GESTOR")
    @ApiResponse(responseCode = "403", description = "Role insuficiente")
    @ApiResponse(responseCode = "404", description = "Ideia não encontrada")
    @ApiResponse(responseCode = "409", description = "Ideia não está em SUBMETIDA ou EM_ANALISE")
    public ResponseEntity<IdeiaResponse> decidir(
            @PathVariable String id,
            @Valid @RequestBody StatusIdeiaRequest request) {
        return ResponseEntity.ok(IdeiaResponse.de(ideiaService.decidir(id, request)));
    }
}
