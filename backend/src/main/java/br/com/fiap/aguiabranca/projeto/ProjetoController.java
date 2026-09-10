package br.com.fiap.aguiabranca.projeto;

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

import br.com.fiap.aguiabranca.projeto.dto.AtualizarProjetoRequest;
import br.com.fiap.aguiabranca.projeto.dto.CriarProjetoRequest;
import br.com.fiap.aguiabranca.projeto.dto.ProgressoRequest;
import br.com.fiap.aguiabranca.projeto.dto.ProjetoResponse;
import br.com.fiap.aguiabranca.projeto.dto.ResultadoRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/projetos")
@RequiredArgsConstructor
@Tag(name = "Projetos")
public class ProjetoController {

    private final ProjetoService projetoService;

    @PostMapping
    @Operation(summary = "Cria um projeto — exclusivo do GESTOR")
    @ApiResponse(responseCode = "403", description = "Role insuficiente")
    @ApiResponse(responseCode = "404", description = "Estratégia ou ideia de origem não encontrada")
    @ApiResponse(responseCode = "409", description = "Ideia de origem já vinculada a outro projeto")
    public ResponseEntity<ProjetoResponse> criar(@Valid @RequestBody CriarProjetoRequest request) {
        Projeto projeto = projetoService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ProjetoResponse.de(projeto));
    }

    @GetMapping
    @Operation(summary = "Lista projetos com filtros opcionais")
    public ResponseEntity<Page<ProjetoResponse>> listar(
            @RequestParam(required = false) EtapaProjeto etapa,
            @RequestParam(required = false) StatusProjeto status,
            @RequestParam(required = false) String estrategiaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProjetoResponse> pagina = projetoService.listar(etapa, status, estrategiaId, pageable)
                .map(ProjetoResponse::de);
        return ResponseEntity.ok(pagina);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhe de um projeto")
    @ApiResponse(responseCode = "404", description = "Projeto não encontrado")
    public ResponseEntity<ProjetoResponse> buscarPorId(@PathVariable String id) {
        return ResponseEntity.ok(ProjetoResponse.de(projetoService.buscarPorId(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza os dados de cadastro de um projeto — exclusivo do GESTOR")
    @ApiResponse(responseCode = "403", description = "Role insuficiente")
    @ApiResponse(responseCode = "404", description = "Projeto ou estratégia não encontrada")
    public ResponseEntity<ProjetoResponse> atualizar(
            @PathVariable String id,
            @Valid @RequestBody AtualizarProjetoRequest request) {
        return ResponseEntity.ok(ProjetoResponse.de(projetoService.atualizar(id, request)));
    }

    @PatchMapping("/{id}/progresso")
    @Operation(summary = "Atualiza etapa/status/percentual de um projeto — exclusivo do GESTOR")
    @ApiResponse(responseCode = "403", description = "Role insuficiente")
    @ApiResponse(responseCode = "404", description = "Projeto não encontrado")
    public ResponseEntity<ProjetoResponse> atualizarProgresso(
            @PathVariable String id,
            @Valid @RequestBody ProgressoRequest request) {
        return ResponseEntity.ok(ProjetoResponse.de(projetoService.atualizarProgresso(id, request)));
    }

    @PostMapping("/{id}/resultados")
    @Operation(summary = "Registra um resultado — agrega ao histórico, exclusivo do GESTOR")
    @ApiResponse(responseCode = "403", description = "Role insuficiente")
    @ApiResponse(responseCode = "404", description = "Projeto não encontrado")
    public ResponseEntity<ProjetoResponse> registrarResultado(
            @PathVariable String id,
            @Valid @RequestBody ResultadoRequest request) {
        return ResponseEntity.ok(ProjetoResponse.de(projetoService.registrarResultado(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um projeto — exclusivo do GESTOR")
    @ApiResponse(responseCode = "403", description = "Role insuficiente")
    @ApiResponse(responseCode = "404", description = "Projeto não encontrado")
    public ResponseEntity<Void> excluir(@PathVariable String id) {
        projetoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
