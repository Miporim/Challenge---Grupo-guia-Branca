package br.com.fiap.aguiabranca.ia;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.aguiabranca.ia.dto.AnaliseIaResponse;
import br.com.fiap.aguiabranca.ia.dto.AnaliseLoteRequest;
import br.com.fiap.aguiabranca.ia.dto.AnaliseLoteResponse;
import br.com.fiap.aguiabranca.ia.dto.ChatRequest;
import br.com.fiap.aguiabranca.ia.dto.ConversaResponse;
import br.com.fiap.aguiabranca.ia.dto.InsightsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Não conhece Gemini/Groq/Ollama — só {@code IaService} (seção 7.4).
 */
@RestController
@RequestMapping("/api/ia")
@RequiredArgsConstructor
@Tag(name = "IA")
public class IaController {

    private final IaService iaService;

    @PostMapping("/ideias/{id}/analise")
    @Operation(summary = "Pontua uma ideia com IA — exclusivo do GESTOR")
    @ApiResponse(responseCode = "403", description = "Role insuficiente")
    @ApiResponse(responseCode = "404", description = "Ideia não encontrada")
    @ApiResponse(responseCode = "502", description = "Resposta da IA não passou na validação do schema")
    @ApiResponse(responseCode = "503", description = "IA indisponível (sem key configurada ou fora do ar)")
    public ResponseEntity<AnaliseIaResponse> analisarIdeia(@PathVariable String id) {
        return ResponseEntity.ok(AnaliseIaResponse.de(iaService.analisarIdeia(id)));
    }

    @PostMapping("/ideias/analise-lote")
    @Operation(summary = "Pontua todas as SUBMETIDA de uma estratégia — exclusivo do GESTOR")
    @ApiResponse(responseCode = "403", description = "Role insuficiente")
    public ResponseEntity<AnaliseLoteResponse> analisarLote(@Valid @RequestBody AnaliseLoteRequest request) {
        return ResponseEntity.ok(iaService.analisarLote(request.estrategiaId()));
    }

    @PostMapping("/chat")
    @Operation(summary = "Chat assistente sobre a estratégia vigente — exclusivo do GESTOR")
    @ApiResponse(responseCode = "403", description = "Role insuficiente ou conversa de outro gestor")
    @ApiResponse(responseCode = "502", description = "Resposta da IA inválida")
    @ApiResponse(responseCode = "503", description = "IA indisponível")
    public ResponseEntity<ConversaResponse> chat(@Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(iaService.chat(request));
    }

    @GetMapping("/chat/conversas")
    @Operation(summary = "Lista as conversas do GESTOR autenticado")
    public ResponseEntity<Page<ConversaResponse>> listarConversas(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(iaService.listarConversas(pageable));
    }

    @GetMapping("/chat/conversas/{id}")
    @Operation(summary = "Detalhe de uma conversa — só o GESTOR dono")
    @ApiResponse(responseCode = "403", description = "Conversa de outro gestor")
    @ApiResponse(responseCode = "404", description = "Conversa não encontrada")
    public ResponseEntity<ConversaResponse> buscarConversa(@PathVariable String id) {
        return ResponseEntity.ok(iaService.buscarConversa(id));
    }

    @PostMapping("/insights")
    @Operation(summary = "Insights do dashboard a partir dos relatórios — exclusivo do LIDER, cacheado 1h")
    @ApiResponse(responseCode = "403", description = "Role insuficiente")
    @ApiResponse(responseCode = "502", description = "Resposta da IA inválida")
    @ApiResponse(responseCode = "503", description = "IA indisponível")
    public ResponseEntity<InsightsResponse> insights() {
        return ResponseEntity.ok(iaService.insights());
    }
}
