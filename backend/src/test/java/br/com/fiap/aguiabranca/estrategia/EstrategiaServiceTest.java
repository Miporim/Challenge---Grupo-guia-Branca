package br.com.fiap.aguiabranca.estrategia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import br.com.fiap.aguiabranca.auditoria.AuditoriaPublisher;
import br.com.fiap.aguiabranca.estrategia.dto.EstrategiaRequest;
import br.com.fiap.aguiabranca.ideia.IdeiaRepository;
import br.com.fiap.aguiabranca.projeto.ProjetoRepository;
import br.com.fiap.aguiabranca.shared.ConflitoException;
import br.com.fiap.aguiabranca.shared.RecursoNaoEncontradoException;

/**
 * Testes de regra de negócio puros (sem Spring context) — o
 * {@code @PreAuthorize} do service tem sua própria prova em
 * {@link EstrategiaServiceMethodSecurityTest}.
 */
class EstrategiaServiceTest {

    private final EstrategiaRepository estrategiaRepository = mock(EstrategiaRepository.class);
    private final MongoTemplate mongoTemplate = mock(MongoTemplate.class);
    private final IdeiaRepository ideiaRepository = mock(IdeiaRepository.class);
    private final ProjetoRepository projetoRepository = mock(ProjetoRepository.class);
    private final AuditoriaPublisher auditoriaPublisher = mock(AuditoriaPublisher.class);
    private final EstrategiaService estrategiaService =
            new EstrategiaService(estrategiaRepository, mongoTemplate, ideiaRepository, projetoRepository, auditoriaPublisher);

    @BeforeEach
    void autenticarComoLider() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("lider-1", null, List.of()));
    }

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    private EstrategiaRequest requestVigente(String campanha) {
        return new EstrategiaRequest("Título", "Descrição", Categoria.EFICIENCIA, campanha,
                Instant.now(), Instant.now().plusSeconds(3600), EstrategiaStatus.VIGENTE);
    }

    @Test
    void criarComStatusVigenteEncerraAnteriorDaMesmaCampanha() {
        Estrategia anterior = Estrategia.builder()
                .id("antiga-1")
                .campanha("Ciclo 2026/1")
                .status(EstrategiaStatus.VIGENTE)
                .build();

        when(estrategiaRepository.save(any(Estrategia.class))).thenAnswer(inv -> {
            Estrategia e = inv.getArgument(0);
            if (e.getId() == null) {
                e.setId("nova-1");
            }
            return e;
        });
        when(estrategiaRepository.findByCampanhaAndStatus("Ciclo 2026/1", EstrategiaStatus.VIGENTE))
                .thenReturn(List.of(anterior));

        estrategiaService.criar(requestVigente("Ciclo 2026/1"));

        assertEquals(EstrategiaStatus.ENCERRADA, anterior.getStatus());
        verify(estrategiaRepository, times(1)).save(anterior);
    }

    @Test
    void criarComStatusEncerradaNaoEncerraOutras() {
        when(estrategiaRepository.save(any(Estrategia.class))).thenAnswer(inv -> inv.getArgument(0));

        EstrategiaRequest request = new EstrategiaRequest("T", "D", Categoria.CUSTO, "Ciclo 2026/1",
                Instant.now(), null, EstrategiaStatus.ENCERRADA);

        estrategiaService.criar(request);

        verify(estrategiaRepository, times(0)).findByCampanhaAndStatus(any(), eq(EstrategiaStatus.VIGENTE));
    }

    @Test
    void atualizarParaVigenteEncerraOutraDaCampanhaMasNaoAPropria() {
        Estrategia existente = Estrategia.builder()
                .id("id-1")
                .campanha("Ciclo 2026/1")
                .status(EstrategiaStatus.ENCERRADA)
                .build();
        Estrategia outraVigente = Estrategia.builder()
                .id("id-2")
                .campanha("Ciclo 2026/1")
                .status(EstrategiaStatus.VIGENTE)
                .build();

        when(estrategiaRepository.findById("id-1")).thenReturn(java.util.Optional.of(existente));
        when(estrategiaRepository.save(any(Estrategia.class))).thenAnswer(inv -> inv.getArgument(0));
        when(estrategiaRepository.findByCampanhaAndStatus("Ciclo 2026/1", EstrategiaStatus.VIGENTE))
                .thenReturn(List.of(existente, outraVigente));

        estrategiaService.atualizar("id-1", requestVigente("Ciclo 2026/1"));

        assertEquals(EstrategiaStatus.ENCERRADA, outraVigente.getStatus());
        // A própria estratégia é salva uma vez pela atualização normal — o
        // laço que encerra as outras vigentes da campanha não deve salvá-la
        // de novo (ela não é "outra").
        verify(estrategiaRepository, times(1)).save(existente);
        verify(estrategiaRepository, times(1)).save(outraVigente);
    }

    @Test
    void excluirComIdeiasVinculadasLancaConflito() {
        Estrategia estrategia = Estrategia.builder().id("id-1").build();
        when(estrategiaRepository.findById("id-1")).thenReturn(java.util.Optional.of(estrategia));
        when(ideiaRepository.existsByEstrategiaId("id-1")).thenReturn(true);

        assertThrows(ConflitoException.class, () -> estrategiaService.excluir("id-1"));
        verify(estrategiaRepository, times(0)).delete(any(Estrategia.class));
    }

    @Test
    void excluirComProjetosVinculadosLancaConflito() {
        Estrategia estrategia = Estrategia.builder().id("id-1").build();
        when(estrategiaRepository.findById("id-1")).thenReturn(java.util.Optional.of(estrategia));
        when(ideiaRepository.existsByEstrategiaId("id-1")).thenReturn(false);
        when(projetoRepository.existsByEstrategiaId("id-1")).thenReturn(true);

        assertThrows(ConflitoException.class, () -> estrategiaService.excluir("id-1"));
        verify(estrategiaRepository, times(0)).delete(any(Estrategia.class));
    }

    @Test
    void excluirSemIdeiasVinculadasExclui() {
        Estrategia estrategia = Estrategia.builder().id("id-1").build();
        when(estrategiaRepository.findById("id-1")).thenReturn(java.util.Optional.of(estrategia));
        when(ideiaRepository.existsByEstrategiaId("id-1")).thenReturn(false);

        estrategiaService.excluir("id-1");

        verify(estrategiaRepository, times(1)).delete(estrategia);
    }

    @Test
    void buscarPorIdInexistenteLancaNaoEncontrado() {
        when(estrategiaRepository.findById("inexistente")).thenReturn(java.util.Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> estrategiaService.buscarPorId("inexistente"));
    }

    @Test
    void vigenteAtualSemNenhumaVigenteLancaNaoEncontrado() {
        when(estrategiaRepository.findFirstByStatusOrderByVigenciaInicioDesc(EstrategiaStatus.VIGENTE))
                .thenReturn(java.util.Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, estrategiaService::vigenteAtual);
    }
}
