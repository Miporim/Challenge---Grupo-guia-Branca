package br.com.fiap.aguiabranca.auditoria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {@code userId} é nullable — um {@code LOGIN_FALHA} para um e-mail que
 * não existe não tem usuário identificável, só o e-mail tentado.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ator {
    private String userId;
    private String email;
    private String role;
}
