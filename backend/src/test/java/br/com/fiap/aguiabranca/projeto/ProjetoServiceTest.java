package br.com.fiap.aguiabranca.projeto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import br.com.fiap.aguiabranca.estrategia.Estrategia;
import br.com.fiap.aguiabranca.estrategia.EstrategiaService;
import br.com.fiap.aguiabranca.estrategia.EstrategiaStatus;
import br.com.fiap.aguiabranca.ideia.Ideia;
import br.com.fiap.aguiabranca.ideia.IdeiaService;
import br.com.fiap.aguiabranca.projeto.dto.CriarProjetoRequest;
import br.com.fiap.aguiabranca.projeto.dto.ProgressoRequest;
import br.com.fiap.aguiabranca.projeto.dto.ResultadoRequest;
import br.com.fiap.aguiabranca.shared.ConflitoException;
import br.com.fiap.aguiabranca.shared.RecursoNaoEncontradoException;
import br.com.fiap.aguiabranca.shared.RequisicaoInvalidaException;

/**
 * Regras de negócio puras (sem Spring context) — a matriz de acesso tem
 * sua própria prova em {@link ProjetoServiceMethodSecurityTest}.
 */
class ProjetoServiceTest {

    private final ProjetoRepository projetoRepository = mock(ProjetoRepository.class);
    private final EstrategiaService estrategiaService = mock(EstrategiaService.class);
    private final IdeiaService ideiaService = mock(IdeiaService.class);
    private final MongoTemplate mongoTemplate = mock(MongoTemplate.class);
    private final ProjetoService projetoService =
            new ProjetoService(projetoRepository, estrategiaService, ideiaService, mongoTemplate);

    private final Estrategia estrategia = Estrategia.builder().id("estrategia-1").status(EstrategiaStatus.VIGENTE).build();

    @BeforeEach
    void autenticarComoGestor() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("gestor-1", null, List.of()));
        when(estrategiaService.buscarPorId("estrategia-1")).thenReturn(estrategia);
        when(projetoRepository.save(any(Projeto.class))).thenAnswer(inv -> {
            Projeto p = inv.getArgument(0);
            if (p.getId() == null) {
                p.setId("projeto-1");
            }
            return p;
        });
    }

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    private CriarProjetoRequest requestValido(String ideiaOrigemId) {
        return new CriarProjetoRequest("Título", "Descrição", "estrategia-1", ideiaOrigemId,
                new BigDecimal("1000"), Instant.now(), Instant.now().plusSeconds(3600), null, null);
    }

    @Test
    void criarComPrazoFimAntesDoInicioLancaRequisicaoInvalida() {
        CriarProjetoRequest request = new CriarProjetoRequest("T", "D", "estrategia-1", null,
                BigDecimal.TEN, Instant.now(), Instant.now().minusSeconds(3600), null, null);

        assertThrows(RequisicaoInvalidaException.class, () -> projetoService.criar(request));
    }

    @Test
    void criarSemEtapaNemStatusUsaDefaults() {
        Projeto projeto = projetoService.criar(requestValido(null));

        assertEquals(EtapaProjeto.PLANEJAMENTO, projeto.getEtapa());
        assertEquals(StatusProjeto.NO_PRAZO, projeto.getStatus());
        assertEquals("gestor-1", projeto.getGestorId());
        assertEquals(0, projeto.getPercentualConcluido());
    }

    @Test
    void criarComIdeiaOrigemJaVinculadaLancaConflito() {
        Ideia ideiaJaVinculada = Ideia.builder().id("ideia-1").projetoId("outro-projeto").build();
        when(ideiaService.buscarPorId("ideia-1")).thenReturn(ideiaJaVinculada);

        assertThrows(ConflitoException.class, () -> projetoService.criar(requestValido("ideia-1")));
    }

    @Test
    void criarComIdeiaOrigemLivreVinculaAoProjetoCriado() {
        Ideia ideiaLivre = Ideia.builder().id("ideia-1").projetoId(null).build();
        when(ideiaService.buscarPorId("ideia-1")).thenReturn(ideiaLivre);

        Projeto projeto = projetoService.criar(requestValido("ideia-1"));

        verify(ideiaService, times(1)).vincularProjeto("ideia-1", projeto.getId());
    }

    @Test
    void atualizarProgressoParaConcluidoForcaPercentualCemEIgnoraValorEnviado() {
        Projeto projeto = Projeto.builder().id("id-1").etapa(EtapaProjeto.EXECUCAO).percentualConcluido(40).build();
        when(projetoRepository.findById("id-1")).thenReturn(java.util.Optional.of(projeto));
        when(projetoRepository.save(any(Projeto.class))).thenAnswer(inv -> inv.getArgument(0));

        Projeto resultado = projetoService.atualizarProgresso("id-1",
                new ProgressoRequest(EtapaProjeto.CONCLUIDO, null, 50));

        assertEquals(100, resultado.getPercentualConcluido());
        assertNotNull(resultado.getConcluidoEm());
    }

    @Test
    void atualizarProgressoSoPercentualAtualizaSemMudarEtapa() {
        Projeto projeto = Projeto.builder().id("id-1").etapa(EtapaProjeto.EXECUCAO).percentualConcluido(40).build();
        when(projetoRepository.findById("id-1")).thenReturn(java.util.Optional.of(projeto));
        when(projetoRepository.save(any(Projeto.class))).thenAnswer(inv -> inv.getArgument(0));

        Projeto resultado = projetoService.atualizarProgresso("id-1", new ProgressoRequest(null, null, 70));

        assertEquals(EtapaProjeto.EXECUCAO, resultado.getEtapa());
        assertEquals(70, resultado.getPercentualConcluido());
    }

    @Test
    void registrarResultadoAgregaSemSobrescrever() {
        Projeto projeto = Projeto.builder().id("id-1").resultados(new java.util.ArrayList<>()).build();
        when(projetoRepository.findById("id-1")).thenReturn(java.util.Optional.of(projeto));
        when(projetoRepository.save(any(Projeto.class))).thenAnswer(inv -> inv.getArgument(0));

        ResultadoRequest r1 = new ResultadoRequest(Instant.now(), BigDecimal.TEN, BigDecimal.ONE, BigDecimal.ZERO, "primeiro");
        ResultadoRequest r2 = new ResultadoRequest(Instant.now(), BigDecimal.TEN, BigDecimal.ONE, BigDecimal.ZERO, "segundo");

        projetoService.registrarResultado("id-1", r1);
        Projeto resultado = projetoService.registrarResultado("id-1", r2);

        assertEquals(2, resultado.getResultados().size());
        assertEquals("primeiro", resultado.getResultados().get(0).getObservacao());
        assertEquals("segundo", resultado.getResultados().get(1).getObservacao());
    }

    @Test
    void buscarPorIdInexistenteLancaNaoEncontrado() {
        when(projetoRepository.findById("inexistente")).thenReturn(java.util.Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> projetoService.buscarPorId("inexistente"));
    }
}
