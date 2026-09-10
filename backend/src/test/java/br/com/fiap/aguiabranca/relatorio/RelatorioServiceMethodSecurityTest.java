package br.com.fiap.aguiabranca.relatorio;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import br.com.fiap.aguiabranca.estrategia.EstrategiaRepository;
import br.com.fiap.aguiabranca.ideia.IdeiaRepository;
import br.com.fiap.aguiabranca.projeto.Projeto;
import br.com.fiap.aguiabranca.projeto.ProjetoService;

/**
 * Prova da matriz de acesso de {@link RelatorioService}. Os caminhos
 * "permitidos" só verificam que nenhuma excepição de autorização é
 * lançada — os números em si (o cálculo do pipeline de agregação) não
 * têm cobertura automatizada nesta etapa (ver
 * {@link RelatorioServiceFormulaTest} para as fórmulas isoladas).
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        RelatorioServiceMethodSecurityTest.SecurityTestConfig.class,
        RelatorioService.class
})
class RelatorioServiceMethodSecurityTest {

    @EnableMethodSecurity
    @Configuration
    static class SecurityTestConfig {
    }

    @MockitoBean
    private MongoTemplate mongoTemplate;

    @MockitoBean
    private EstrategiaRepository estrategiaRepository;

    @MockitoBean
    private IdeiaRepository ideiaRepository;

    @MockitoBean
    private ProjetoService projetoService;

    @Autowired
    private RelatorioService relatorioService;

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void permitirQualquerAgregacaoVazia() {
        AggregationResults<Map> vazio = new AggregationResults<>(List.of(), new Document());
        when(mongoTemplate.aggregate(org.mockito.ArgumentMatchers.any(Aggregation.class), anyString(), eq(Map.class)))
                .thenReturn(vazio);
        when(mongoTemplate.count(any(), eq(Projeto.class))).thenReturn(0L);
        when(estrategiaRepository.findAll()).thenReturn(List.of());
    }

    private void autenticarComo(String usuarioId, String role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                usuarioId, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void gestorConsegueVerResumo() {
        autenticarComo("gestor-1", "GESTOR");
        permitirQualquerAgregacaoVazia();

        assertDoesNotThrow(relatorioService::resumo);
    }

    @Test
    void liderConsegueVerResumo() {
        autenticarComo("lider-1", "LIDER");
        permitirQualquerAgregacaoVazia();

        assertDoesNotThrow(relatorioService::resumo);
    }

    @Test
    void operadorNaoConsegueVerResumo() {
        autenticarComo("operador-1", "OPERADOR");
        assertThrows(AccessDeniedException.class, relatorioService::resumo);
    }

    @Test
    void operadorNaoConsegueVerPorEstrategia() {
        autenticarComo("operador-1", "OPERADOR");
        assertThrows(AccessDeniedException.class, relatorioService::porEstrategia);
    }

    @Test
    void gestorConsegueVerProjetoDetalhe() {
        autenticarComo("gestor-1", "GESTOR");
        when(projetoService.buscarPorId("id-1")).thenReturn(
                Projeto.builder().id("id-1").resultados(List.of()).build());

        assertDoesNotThrow(() -> relatorioService.projetoDetalhe("id-1"));
    }

    @Test
    void operadorNaoConsegueVerProjetoDetalhe() {
        autenticarComo("operador-1", "OPERADOR");
        assertThrows(AccessDeniedException.class, () -> relatorioService.projetoDetalhe("id-1"));
    }

    @Test
    void liderConsegueVerSerieTemporal() {
        autenticarComo("lider-1", "LIDER");
        permitirQualquerAgregacaoVazia();

        assertDoesNotThrow(() -> relatorioService.serieTemporal(Instant.now().minusSeconds(3600), Instant.now()));
    }

    @Test
    void gestorNaoConsegueVerSerieTemporal() {
        autenticarComo("gestor-1", "GESTOR");
        assertThrows(AccessDeniedException.class,
                () -> relatorioService.serieTemporal(Instant.now().minusSeconds(3600), Instant.now()));
    }

    @Test
    void gestorConsegueVerFunil() {
        autenticarComo("gestor-1", "GESTOR");
        permitirQualquerAgregacaoVazia();
        when(ideiaRepository.countByStatusNot(any())).thenReturn(0L);
        when(ideiaRepository.countByStatus(any())).thenReturn(0L);
        when(ideiaRepository.countByProjetoIdIsNotNull()).thenReturn(0L);

        assertDoesNotThrow(relatorioService::funil);
    }

    @Test
    void operadorNaoConsegueVerFunil() {
        autenticarComo("operador-1", "OPERADOR");
        assertThrows(AccessDeniedException.class, relatorioService::funil);
    }
}
