package br.com.fiap.aguiabranca.auditoria;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Só os campos que mudaram — nunca o documento inteiro (seção 6). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alteracoes {
    private Map<String, Object> antes;
    private Map<String, Object> depois;
}
