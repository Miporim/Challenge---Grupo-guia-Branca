package br.com.fiap.aguiabranca.relatorio;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.DateOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import br.com.fiap.aguiabranca.estrategia.Estrategia;
import br.com.fiap.aguiabranca.estrategia.EstrategiaRepository;
import br.com.fiap.aguiabranca.ideia.IdeiaRepository;
import br.com.fiap.aguiabranca.ideia.StatusIdeia;
import br.com.fiap.aguiabranca.projeto.EtapaProjeto;
import br.com.fiap.aguiabranca.projeto.Projeto;
import br.com.fiap.aguiabranca.projeto.ProjetoService;
import br.com.fiap.aguiabranca.projeto.StatusProjeto;
import br.com.fiap.aguiabranca.projeto.dto.ProjetoResponse;
import br.com.fiap.aguiabranca.relatorio.dto.FunilResponse;
import br.com.fiap.aguiabranca.relatorio.dto.PorEstrategiaResponse;
import br.com.fiap.aguiabranca.relatorio.dto.ProjetoDetalheResponse;
import br.com.fiap.aguiabranca.relatorio.dto.ResumoResponse;
import br.com.fiap.aguiabranca.relatorio.dto.SerieTemporalItem;
import lombok.RequiredArgsConstructor;

/**
 * Todos os relatórios usam {@code Aggregation} do Spring Data Mongo — soma,
 * média e contagem são calculadas no banco, nunca carregando listas de
 * projetos/ideias na aplicação para somar em Java (seção 4, ponto
 * explícito da avaliação).
 *
 * Fórmulas (seção 4): {@code retornoTotal = Σreceita + Σeconomia}, {@code
 * lucro = retornoTotal - investimento}, {@code roi% = lucro/investimento
 * × 100} (0 se investimento = 0). Tudo arredondado com HALF_UP em 2
 * casas. Nunca {@code null} — 0 quando não há dados.
 */
@Service
@RequiredArgsConstructor
public class RelatorioService {

    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private final MongoTemplate mongoTemplate;
    private final EstrategiaRepository estrategiaRepository;
    private final IdeiaRepository ideiaRepository;
    private final ProjetoService projetoService;

    @PreAuthorize("hasRole('GESTOR') or hasRole('LIDER')")
    public ResumoResponse resumo() {
        return montarResumo(null);
    }

    @PreAuthorize("hasRole('GESTOR') or hasRole('LIDER')")
    public List<PorEstrategiaResponse> porEstrategia() {
        List<Estrategia> estrategias = estrategiaRepository.findAll();
        return estrategias.stream()
                .map(estrategia -> {
                    ResumoResponse resumo = montarResumo(Criteria.where("estrategiaId").is(estrategia.getId()));
                    return new PorEstrategiaResponse(
                            estrategia.getId(),
                            estrategia.getTitulo(),
                            estrategia.getCategoria(),
                            resumo.totalProjetos(),
                            resumo.investimentoTotal(),
                            resumo.retornoTotal(),
                            resumo.lucro(),
                            resumo.roiPercentual(),
                            resumo.prazoMedioDias(),
                            resumo.ganhoProdutividadeMedio(),
                            resumo.porEtapa(),
                            resumo.porStatus()
                    );
                })
                .toList();
    }

    @PreAuthorize("hasRole('GESTOR') or hasRole('LIDER')")
    public ProjetoDetalheResponse projetoDetalhe(String projetoId) {
        Projeto projeto = projetoService.buscarPorId(projetoId);

        BigDecimal retornoTotal = projeto.getResultados().stream()
                .map(r -> somaSegura(r.getReceita()).add(somaSegura(r.getEconomia())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal investimento = somaSegura(projeto.getInvestimento());
        BigDecimal lucro = retornoTotal.subtract(investimento);
        BigDecimal roi = calcularRoi(lucro, investimento);

        return new ProjetoDetalheResponse(
                ProjetoResponse.de(projeto),
                arredondar(retornoTotal),
                arredondar(lucro),
                roi
        );
    }

    @PreAuthorize("hasRole('LIDER')")
    public List<SerieTemporalItem> serieTemporal(Instant de, Instant ate) {
        // DECISION: só a granularidade MES está implementada — é a única
        // usada no exemplo da seção 4. Um valor diferente é ignorado e
        // tratado como MES, para não travar o dashboard.
        Map<String, BigDecimal> investimentoPorMes = new LinkedHashMap<>();
        Map<String, BigDecimal> retornoPorMes = new LinkedHashMap<>();

        var pipelineInvestimento = newAggregation(
                match(Criteria.where("prazoInicio").gte(de).lte(ate)),
                project()
                        .and(DateOperators.dateOf("prazoInicio").toString("%Y-%m")).as("periodo")
                        .and("investimento").as("investimento"),
                group("periodo").sum("investimento").as("total"),
                sort(Sort.Direction.ASC, "_id")
        );
        AggregationResults<Map> resultadoInvestimento = mongoTemplate.aggregate(pipelineInvestimento, "projetos", Map.class);
        for (Map doc : resultadoInvestimento.getMappedResults()) {
            investimentoPorMes.put(String.valueOf(doc.get("_id")), toBigDecimal(doc.get("total")));
        }

        var pipelineRetorno = newAggregation(
                unwind("resultados"),
                match(Criteria.where("resultados.data").gte(de).lte(ate)),
                project()
                        .and(DateOperators.dateOf("resultados.data").toString("%Y-%m")).as("periodo")
                        .andExpression("resultados.receita + resultados.economia").as("retornoLinha"),
                group("periodo").sum("retornoLinha").as("total"),
                sort(Sort.Direction.ASC, "_id")
        );
        AggregationResults<Map> resultadoRetorno = mongoTemplate.aggregate(pipelineRetorno, "projetos", Map.class);
        for (Map doc : resultadoRetorno.getMappedResults()) {
            retornoPorMes.put(String.valueOf(doc.get("_id")), toBigDecimal(doc.get("total")));
        }

        java.util.TreeSet<String> periodos = new java.util.TreeSet<>();
        periodos.addAll(investimentoPorMes.keySet());
        periodos.addAll(retornoPorMes.keySet());

        return periodos.stream()
                .map(periodo -> new SerieTemporalItem(
                        periodo,
                        arredondar(investimentoPorMes.getOrDefault(periodo, BigDecimal.ZERO)),
                        arredondar(retornoPorMes.getOrDefault(periodo, BigDecimal.ZERO))))
                .toList();
    }

    @PreAuthorize("hasRole('GESTOR') or hasRole('LIDER')")
    public FunilResponse funil() {
        // DECISION: "submetidas" = toda ideia que não ficou só em RASCUNHO
        // (já passou por SUBMETIDA em algum momento); não há histórico de
        // transição de estado persistido, só o status atual.
        long submetidas = ideiaRepository.countByStatusNot(StatusIdeia.RASCUNHO);
        long aprovadas = ideiaRepository.countByStatus(StatusIdeia.APROVADA);
        long viraramProjeto = ideiaRepository.countByProjetoIdIsNotNull();
        long concluidas = mongoTemplate.count(
                org.springframework.data.mongodb.core.query.Query.query(
                        Criteria.where("ideiaOrigemId").ne(null).and("etapa").is(EtapaProjeto.CONCLUIDO)),
                Projeto.class);

        return new FunilResponse(submetidas, aprovadas, viraramProjeto, concluidas);
    }

    private ResumoResponse montarResumo(Criteria filtroOpcional) {
        Criteria base = filtroOpcional != null ? filtroOpcional : new Criteria();

        long totalProjetos = mongoTemplate.count(
                org.springframework.data.mongodb.core.query.Query.query(base), Projeto.class);
        BigDecimal investimentoTotal = somarCampo(base, "investimento");
        BigDecimal retornoTotal = somarRetorno(base);
        BigDecimal lucro = retornoTotal.subtract(investimentoTotal);
        BigDecimal roi = calcularRoi(lucro, investimentoTotal);
        BigDecimal prazoMedioDias = calcularPrazoMedioDias(base);
        BigDecimal ganhoProdutividadeMedio = calcularGanhoProdutividadeMedio(base);
        Map<EtapaProjeto, Long> porEtapa = contarPor(base, "etapa", EtapaProjeto.class);
        Map<StatusProjeto, Long> porStatus = contarPor(base, "status", StatusProjeto.class);

        return new ResumoResponse(
                totalProjetos,
                arredondar(investimentoTotal),
                arredondar(retornoTotal),
                arredondar(lucro),
                roi,
                prazoMedioDias,
                ganhoProdutividadeMedio,
                porEtapa,
                porStatus
        );
    }

    private BigDecimal somarCampo(Criteria filtro, String campo) {
        var pipeline = newAggregation(
                match(filtro),
                group().sum(campo).as("total")
        );
        AggregationResults<Map> resultado = mongoTemplate.aggregate(pipeline, "projetos", Map.class);
        Map doc = resultado.getUniqueMappedResult();
        return doc == null ? BigDecimal.ZERO : toBigDecimal(doc.get("total"));
    }

    private BigDecimal somarRetorno(Criteria filtroProjeto) {
        var pipeline = newAggregation(
                match(filtroProjeto),
                unwind("resultados"),
                project().andExpression("resultados.receita + resultados.economia").as("retornoLinha"),
                group().sum("retornoLinha").as("total")
        );
        AggregationResults<Map> resultado = mongoTemplate.aggregate(pipeline, "projetos", Map.class);
        Map doc = resultado.getUniqueMappedResult();
        return doc == null ? BigDecimal.ZERO : toBigDecimal(doc.get("total"));
    }

    private BigDecimal calcularGanhoProdutividadeMedio(Criteria filtroProjeto) {
        var pipeline = newAggregation(
                match(filtroProjeto),
                unwind("resultados"),
                group().avg("resultados.ganhoProdutividadePct").as("media")
        );
        AggregationResults<Map> resultado = mongoTemplate.aggregate(pipeline, "projetos", Map.class);
        Map doc = resultado.getUniqueMappedResult();
        return doc == null ? BigDecimal.ZERO : arredondar(toBigDecimal(doc.get("media")));
    }

    private BigDecimal calcularPrazoMedioDias(Criteria filtroProjeto) {
        Criteria comConcluidoEm = new Criteria().andOperator(filtroProjeto, Criteria.where("concluidoEm").ne(null));
        var pipeline = newAggregation(
                match(comConcluidoEm),
                project().andExpression("(concluidoEm - prazoInicio) / 86400000").as("dias"),
                group().avg("dias").as("media")
        );
        AggregationResults<Map> resultado = mongoTemplate.aggregate(pipeline, "projetos", Map.class);
        Map doc = resultado.getUniqueMappedResult();
        return doc == null ? BigDecimal.ZERO : arredondar(toBigDecimal(doc.get("media")));
    }

    private <E extends Enum<E>> Map<E, Long> contarPor(Criteria filtro, String campo, Class<E> tipoEnum) {
        var pipeline = newAggregation(
                match(filtro),
                group(campo).count().as("total")
        );
        AggregationResults<Map> resultado = mongoTemplate.aggregate(pipeline, "projetos", Map.class);
        Map<E, Long> contagem = new HashMap<>();
        for (Map doc : resultado.getMappedResults()) {
            Object chave = doc.get("_id");
            if (chave == null) {
                continue;
            }
            E valor = Enum.valueOf(tipoEnum, String.valueOf(chave));
            contagem.put(valor, ((Number) doc.get("total")).longValue());
        }
        return contagem;
    }

    // DECISION: visibilidade de pacote (não private) só para permitir um
    // teste unitário direto das fórmulas (seção 4), sem precisar montar um
    // MongoTemplate real nem mockar toda a cadeia de agregação — ver
    // RelatorioServiceFormulaTest.
    BigDecimal calcularRoi(BigDecimal lucro, BigDecimal investimento) {
        if (investimento.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return arredondar(lucro.divide(investimento, 10, RoundingMode.HALF_UP).multiply(CEM));
    }

    BigDecimal arredondar(BigDecimal valor) {
        return (valor == null ? BigDecimal.ZERO : valor).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal somaSegura(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private BigDecimal toBigDecimal(Object valor) {
        if (valor == null) {
            return BigDecimal.ZERO;
        }
        if (valor instanceof BigDecimal bd) {
            return bd;
        }
        if (valor instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        return new BigDecimal(valor.toString());
    }
}
