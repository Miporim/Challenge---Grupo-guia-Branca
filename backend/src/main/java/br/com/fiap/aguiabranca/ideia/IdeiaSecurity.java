package br.com.fiap.aguiabranca.ideia;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Bean de posse (seção 5): "só o autor edita sua própria ideia" é regra de
 * dado, não de role. Usado em {@code @PreAuthorize} como
 * {@code @ideiaSecurity.ehAutor(#id, authentication)} — mesmo padrão a
 * repetir em {@code projeto} (Etapa D).
 */
@Component("ideiaSecurity")
@RequiredArgsConstructor
public class IdeiaSecurity {

    private final IdeiaRepository ideiaRepository;

    public boolean ehAutor(String ideiaId, Authentication authentication) {
        if (authentication == null || ideiaId == null) {
            return false;
        }
        String usuarioId = String.valueOf(authentication.getPrincipal());
        return ideiaRepository.findById(ideiaId)
                .map(Ideia::getAutorId)
                .map(autorId -> autorId.equals(usuarioId))
                .orElse(false);
    }
}
