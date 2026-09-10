package br.com.fiap.aguiabranca.usuario;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.aguiabranca.usuario.dto.AtivoRequest;
import br.com.fiap.aguiabranca.usuario.dto.CriarUsuarioRequest;
import br.com.fiap.aguiabranca.usuario.dto.UsuarioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Exclusivo do LIDER. A verificação de role é feita em
 * {@code UsuarioService} via {@code @PreAuthorize} — este controller não
 * decide permissão, só mapeia HTTP.
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    @Operation(summary = "Cria um GESTOR ou LIDER — exclusivo do LIDER")
    @ApiResponse(responseCode = "403", description = "Role insuficiente")
    @ApiResponse(responseCode = "409", description = "E-mail já cadastrado")
    public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody CriarUsuarioRequest request) {
        Usuario usuario = usuarioService.criarGestorOuLider(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.de(usuario));
    }

    @GetMapping
    @Operation(summary = "Lista usuários paginados, com filtro opcional por role")
    @ApiResponse(responseCode = "403", description = "Role insuficiente")
    public ResponseEntity<Page<UsuarioResponse>> listar(
            @RequestParam(required = false) Role role,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<UsuarioResponse> pagina = usuarioService.listar(role, pageable).map(UsuarioResponse::de);
        return ResponseEntity.ok(pagina);
    }

    @PatchMapping("/{id}/ativo")
    @Operation(summary = "Ativa ou desativa um usuário — exclusivo do LIDER")
    @ApiResponse(responseCode = "403", description = "Role insuficiente")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    public ResponseEntity<UsuarioResponse> alterarAtivo(
            @PathVariable String id,
            @Valid @RequestBody AtivoRequest request) {
        Usuario usuario = usuarioService.alterarAtivo(id, request.ativo());
        return ResponseEntity.ok(UsuarioResponse.de(usuario));
    }
}
