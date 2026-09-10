package br.com.fiap.aguiabranca.ia;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Posse da conversa (seção 4: "GESTOR — solo las propias") — mesmo
 * padrão de {@code IdeiaSecurity} (Etapa C).
 */
@Component("conversaSecurity")
@RequiredArgsConstructor
public class ConversaSecurity {

    private final ConversaIaRepository conversaIaRepository;

    public boolean ehDono(String conversaId, Authentication authentication) {
        if (authentication == null || conversaId == null) {
            return false;
        }
        String usuarioId = String.valueOf(authentication.getPrincipal());
        return conversaIaRepository.findById(conversaId)
                .map(ConversaIa::getGestorId)
                .map(gestorId -> gestorId.equals(usuarioId))
                .orElse(false);
    }
}
