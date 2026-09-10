package br.com.fiap.aguiabranca.estrategia;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import br.com.fiap.aguiabranca.estrategia.dto.EstrategiaRequest;
import br.com.fiap.aguiabranca.ideia.IdeiaRepository;
import br.com.fiap.aguiabranca.projeto.ProjetoRepository;

/**
 * Prova da matriz de acesso de {@link EstrategiaService} — mesmo padrão de
 * {@code UsuarioServiceMethodSecurityTest} (Etapa A): contexto mínimo, sem
 * autoconfig do Boot, para não depender de Mongo real.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        EstrategiaServiceMethodSecurityTest.SecurityTestConfig.class,
        EstrategiaService.class
})
class EstrategiaServiceMethodSecurityTest {

    @EnableMethodSecurity
    @Configuration
    static class SecurityTestConfig {
    }

    @MockitoBean
    private EstrategiaRepository estrategiaRepository;

    @MockitoBean
    private MongoTemplate mongoTemplate;

    @MockitoBean
    private IdeiaRepository ideiaRepository;

    @MockitoBean
    private ProjetoRepository projetoRepository;

    @Autowired
    private EstrategiaService estrategiaService;

    private EstrategiaRequest requestValido() {
        return new EstrategiaRequest("Título", "Descrição", Categoria.EFICIENCIA, "Ciclo 2026/1",
                Instant.now(), Instant.now().plusSeconds(3600), EstrategiaStatus.ENCERRADA);
    }

    @Test
    @WithMockUser(roles = "LIDER")
    void liderConsegueCriar() {
        when(estrategiaRepository.save(any(Estrategia.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> estrategiaService.criar(requestValido()));
    }

    @Test
    @WithMockUser(roles = "OPERADOR")
    void operadorNaoConsegueCriar() {
        assertThrows(AccessDeniedException.class, () -> estrategiaService.criar(requestValido()));
    }

    @Test
    @WithMockUser(roles = "GESTOR")
    void gestorNaoConsegueCriar() {
        assertThrows(AccessDeniedException.class, () -> estrategiaService.criar(requestValido()));
    }

    @Test
    @WithMockUser(roles = "OPERADOR")
    void operadorNaoConsegueAtualizar() {
        assertThrows(AccessDeniedException.class, () -> estrategiaService.atualizar("id-1", requestValido()));
    }

    @Test
    @WithMockUser(roles = "OPERADOR")
    void operadorNaoConsegueEncerrar() {
        assertThrows(AccessDeniedException.class, () -> estrategiaService.encerrar("id-1"));
    }

    @Test
    @WithMockUser(roles = "OPERADOR")
    void operadorNaoConsegueExcluir() {
        assertThrows(AccessDeniedException.class, () -> estrategiaService.excluir("id-1"));
    }

    @Test
    @WithMockUser(roles = "LIDER")
    void liderConsegueEncerrar() {
        Estrategia estrategia = Estrategia.builder().id("id-1").status(EstrategiaStatus.VIGENTE).build();
        when(estrategiaRepository.findById("id-1")).thenReturn(Optional.of(estrategia));
        when(estrategiaRepository.save(any(Estrategia.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> estrategiaService.encerrar("id-1"));
    }

    @Test
    @WithMockUser(roles = "GESTOR")
    void gestorNaoConsegueListarComPreAuthorizeAusente() {
        // listar() não tem @PreAuthorize — é leitura, liberada por rota para
        // qualquer autenticado. Este teste só confirma que não é bloqueada
        // no service (a rota é garantida pelo SecurityConfig, fora do
        // escopo deste teste).
        when(mongoTemplate.count(any(), eq(Estrategia.class))).thenReturn(0L);
        when(mongoTemplate.find(any(), eq(Estrategia.class))).thenReturn(List.of());

        assertDoesNotThrow(() -> estrategiaService.listar(null, null, null,
                org.springframework.data.domain.PageRequest.of(0, 20)));
    }
}
