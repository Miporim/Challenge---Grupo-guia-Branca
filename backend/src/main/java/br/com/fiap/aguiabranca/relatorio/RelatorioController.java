package br.com.fiap.aguiabranca.relatorio;

import java.time.Instant;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.aguiabranca.relatorio.dto.FunilResponse;
import br.com.fiap.aguiabranca.relatorio.dto.PorEstrategiaResponse;
import br.com.fiap.aguiabranca.relatorio.dto.ProjetoDetalheResponse;
import br.com.fiap.aguiabranca.relatorio.dto.ResumoResponse;
import br.com.fiap.aguiabranca.relatorio.dto.SerieTemporalItem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * Todos os números são calculados via {@code Aggregation} em
 * {@code RelatorioService} — ver seção 4. A verificação de role fica no
 * service.
 */
@RestController
@RequestMapping("/api/relatorios")
@RequiredArgsConstructor
@Tag(name = "Relatórios")
public class RelatorioController {

    private final RelatorioService relatorioService;

    @GetMapping("/resumo")
    @Operation(summary = "Totais consolidados — GESTOR e LIDER")
    @ApiResponse(responseCode = "403", description = "Role insuficiente")
    public ResponseEntity<ResumoResponse> resumo() {
        return ResponseEntity.ok(relatorioService.resumo());
    }

    @GetMapping("/por-estrategia")
    @Operation(summary = "Mesmos totais, agrupados por estratégia — GESTOR e LIDER")
    @ApiResponse(responseCode = "403", description = "Role insuficiente")
    public ResponseEntity<List<PorEstrategiaResponse>> porEstrategia() {
        return ResponseEntity.ok(relatorioService.porEstrategia());
    }

    @GetMapping("/projetos/{id}")
    @Operation(summary = "Detalhe de um projeto com sua série de resultados — GESTOR e LIDER")
    @ApiResponse(responseCode = "403", description = "Role insuficiente")
    @ApiResponse(responseCode = "404", description = "Projeto não encontrado")
    public ResponseEntity<ProjetoDetalheResponse> projetoDetalhe(@PathVariable String id) {
        return ResponseEntity.ok(relatorioService.projetoDetalhe(id));
    }

    @GetMapping("/serie-temporal")
    @Operation(summary = "Investimento x retorno por mês — exclusivo do LIDER")
    @ApiResponse(responseCode = "403", description = "Role insuficiente")
    public ResponseEntity<List<SerieTemporalItem>> serieTemporal(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant de,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant ate,
            @RequestParam(required = false, defaultValue = "MES") String granularidade) {
        return ResponseEntity.ok(relatorioService.serieTemporal(de, ate));
    }

    @GetMapping("/funil")
    @Operation(summary = "Funil submetidas → aprovadas → viraram projeto → concluídas — GESTOR e LIDER")
    @ApiResponse(responseCode = "403", description = "Role insuficiente")
    public ResponseEntity<FunilResponse> funil() {
        return ResponseEntity.ok(relatorioService.funil());
    }
}
