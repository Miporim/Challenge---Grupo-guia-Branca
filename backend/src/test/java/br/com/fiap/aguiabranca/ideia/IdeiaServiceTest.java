package br.com.fiap.aguiabranca.ideia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import br.com.fiap.aguiabranca.estrategia.Estrategia;
import br.com.fiap.aguiabranca.estrategia.EstrategiaService;
import br.com.fiap.aguiabranca.estrategia.EstrategiaStatus;
import br.com.fiap.aguiabranca.ideia.dto.IdeiaRequest;
import br.com.fiap.aguiabranca.ideia.dto.PriorizacaoRequest;
import br.com.fiap.aguiabranca.ideia.dto.StatusIdeiaRequest;
import br.com.fiap.aguiabranca.shared.ConflitoException;
import br.com.fiap.aguiabranca.shared.RequisicaoInvalidaException;

/**
 * Regras de negócio puras (sem Spring context) — a matriz de acesso tem
 * sua própria prova em {@link IdeiaServiceMethodSecurityTest}.
 */
class IdeiaServiceTest {

    private final IdeiaRepository ideiaRepository = mock(IdeiaRepository.class);
    private final EstrategiaService estrategiaService = mock(EstrategiaService.class);
    private final MongoTemplate mongoTemplate = mock(MongoTemplate.class);
    private final IdeiaService ideiaService = new IdeiaService(ideiaRepository, estrategiaService, mongoTemplate);

    private final Estrategia estrategiaVigente = Estrategia.builder()
            .id("estrategia-1")
            .status(EstrategiaStatus.VIGENTE)
            .build();

    @BeforeEach
    void autenticarComoOperador() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("operador-1", null, List.of()));
        when(estrategiaService.buscarPorId("estrategia-1")).thenReturn(estrategiaVigente);
        when(ideiaRepository.save(any(Ideia.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    private IdeiaRequest requestValido(StatusIdeia status) {
        return new IdeiaRequest("Título", "Descrição", "estrategia-1", Nivel.ALTO, Nivel.BAIXO, status);
    }

    @Test
    void criarSemStatusUsaDefaultSubmetida() {
        Ideia ideia = ideiaService.criar(requestValido(null));

        assertEquals(StatusIdeia.SUBMETIDA, ideia.getStatus());
        assertEquals("operador-1", ideia.getAutorId());
        assertEquals(BigDecimal.ZERO, ideia.getNotaMedia());
    }

    @Test
    void criarComStatusAprovadaLancaRequisicaoInvalida() {
        assertThrows(RequisicaoInvalidaException.class, () -> ideiaService.criar(requestValido(StatusIdeia.APROVADA)));
    }

    @Test
    void criarComEstrategiaNaoVigenteLancaRequisicaoInvalida() {
        Estrategia encerrada = Estrategia.builder().id("estrategia-2").status(EstrategiaStatus.ENCERRADA).build();
        when(estrategiaService.buscarPorId("estrategia-2")).thenReturn(encerrada);

        IdeiaRequest request = new IdeiaRequest("T", "D", "estrategia-2", Nivel.ALTO, Nivel.BAIXO, null);

        assertThrows(RequisicaoInvalidaException.class, () -> ideiaService.criar(request));
    }

    @Test
    void atualizarIdeiaAprovadaLancaConflito() {
        Ideia aprovada = Ideia.builder().id("id-1").status(StatusIdeia.APROVADA).build();
        when(ideiaRepository.findById("id-1")).thenReturn(Optional.of(aprovada));

        assertThrows(ConflitoException.class, () -> ideiaService.atualizar("id-1", requestValido(null)));
    }

    @Test
    void excluirIdeiaAprovadaLancaConflito() {
        Ideia aprovada = Ideia.builder().id("id-1").status(StatusIdeia.APROVADA).build();
        when(ideiaRepository.findById("id-1")).thenReturn(Optional.of(aprovada));

        assertThrows(ConflitoException.class, () -> ideiaService.excluir("id-1"));
    }

    @Test
    void excluirIdeiaSubmetidaFunciona() {
        Ideia submetida = Ideia.builder().id("id-1").status(StatusIdeia.SUBMETIDA).build();
        when(ideiaRepository.findById("id-1")).thenReturn(Optional.of(submetida));

        ideiaService.excluir("id-1");
        // não lança — sucesso implícito
    }

    @Test
    void priorizarMoveDeSubmetidaParaEmAnalise() {
        Ideia submetida = Ideia.builder()
                .id("id-1")
                .status(StatusIdeia.SUBMETIDA)
                .priorizacoes(new java.util.ArrayList<>())
                .build();
        when(ideiaRepository.findById("id-1")).thenReturn(Optional.of(submetida));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("gestor-1", null, List.of()));

        Ideia resultado = ideiaService.priorizar("id-1", new PriorizacaoRequest(4, "bom"));

        assertEquals(StatusIdeia.EM_ANALISE, resultado.getStatus());
        assertEquals(1, resultado.getTotalVotos());
        assertEquals(new BigDecimal("4.00"), resultado.getNotaMedia());
    }

    @Test
    void priorizarDuasVezesMesmoGestorAtualizaVotoSemDuplicar() {
        Ideia emAnalise = Ideia.builder()
                .id("id-1")
                .status(StatusIdeia.EM_ANALISE)
                .priorizacoes(new java.util.ArrayList<>())
                .build();
        when(ideiaRepository.findById("id-1")).thenReturn(Optional.of(emAnalise));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("gestor-1", null, List.of()));

        ideiaService.priorizar("id-1", new PriorizacaoRequest(2, "primeiro voto"));
        Ideia resultado = ideiaService.priorizar("id-1", new PriorizacaoRequest(5, "voto corrigido"));

        assertEquals(1, resultado.getTotalVotos());
        assertEquals(new BigDecimal("5.00"), resultado.getNotaMedia());
        assertEquals("voto corrigido", resultado.getPriorizacoes().get(0).getComentario());
    }

    @Test
    void priorizarIdeiaEmRascunhoLancaConflito() {
        Ideia rascunho = Ideia.builder().id("id-1").status(StatusIdeia.RASCUNHO).priorizacoes(List.of()).build();
        when(ideiaRepository.findById("id-1")).thenReturn(Optional.of(rascunho));

        assertThrows(ConflitoException.class,
                () -> ideiaService.priorizar("id-1", new PriorizacaoRequest(3, null)));
    }

    @Test
    void decidirAprovarIdeiaSubmetidaFunciona() {
        Ideia submetida = Ideia.builder().id("id-1").status(StatusIdeia.SUBMETIDA).build();
        when(ideiaRepository.findById("id-1")).thenReturn(Optional.of(submetida));

        Ideia resultado = ideiaService.decidir("id-1", new StatusIdeiaRequest(StatusIdeia.APROVADA, "ok"));

        assertEquals(StatusIdeia.APROVADA, resultado.getStatus());
    }

    @Test
    void decidirIdeiaJaAprovadaLancaConflito() {
        Ideia aprovada = Ideia.builder().id("id-1").status(StatusIdeia.APROVADA).build();
        when(ideiaRepository.findById("id-1")).thenReturn(Optional.of(aprovada));

        assertThrows(ConflitoException.class,
                () -> ideiaService.decidir("id-1", new StatusIdeiaRequest(StatusIdeia.APROVADA, "de novo")));
    }

    @Test
    void decidirComStatusInvalidoLancaRequisicaoInvalida() {
        assertThrows(RequisicaoInvalidaException.class,
                () -> ideiaService.decidir("id-1", new StatusIdeiaRequest(StatusIdeia.RASCUNHO, "x")));
    }
}
