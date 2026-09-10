package br.com.fiap.aguiabranca.seed;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import br.com.fiap.aguiabranca.estrategia.Categoria;
import br.com.fiap.aguiabranca.estrategia.Estrategia;
import br.com.fiap.aguiabranca.estrategia.EstrategiaRepository;
import br.com.fiap.aguiabranca.estrategia.EstrategiaStatus;
import br.com.fiap.aguiabranca.ideia.Ideia;
import br.com.fiap.aguiabranca.ideia.IdeiaRepository;
import br.com.fiap.aguiabranca.ideia.Nivel;
import br.com.fiap.aguiabranca.ideia.Priorizacao;
import br.com.fiap.aguiabranca.ideia.StatusIdeia;
import br.com.fiap.aguiabranca.projeto.EtapaProjeto;
import br.com.fiap.aguiabranca.projeto.Projeto;
import br.com.fiap.aguiabranca.projeto.ProjetoRepository;
import br.com.fiap.aguiabranca.projeto.Resultado;
import br.com.fiap.aguiabranca.projeto.StatusProjeto;
import br.com.fiap.aguiabranca.usuario.Role;
import br.com.fiap.aguiabranca.usuario.Usuario;
import br.com.fiap.aguiabranca.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Carga de dados de exemplo — seção 9: "sem dados, os gráficos da app
 * nascem vazios e não se pode demonstrar nada". Ativado só com
 * {@code --spring.profiles.active=seed} (além do perfil de conexão).
 * Idempotente: não duplica se já houver usuários cadastrados.
 */
@Component
@Profile("seed")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final EstrategiaRepository estrategiaRepository;
    private final IdeiaRepository ideiaRepository;
    private final ProjetoRepository projetoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            log.info("Seed: já há dados carregados, pulando.");
            return;
        }

        Instant agora = Instant.now();

        Usuario operador = usuarioRepository.save(usuario("Operador Exemplo", "operador@aguiabranca.com", Role.OPERADOR, agora));
        Usuario gestor = usuarioRepository.save(usuario("Gestor Exemplo", "gestor@aguiabranca.com", Role.GESTOR, agora));
        Usuario lider = usuarioRepository.save(usuario("Líder Exemplo", "lider@aguiabranca.com", Role.LIDER, agora));

        Estrategia estrategiaVigente = estrategiaRepository.save(Estrategia.builder()
                .titulo("Eficiência Operacional")
                .descricao("Reduzir custos operacionais e otimizar processos de logística.")
                .categoria(Categoria.EFICIENCIA)
                .campanha("Ciclo 2026/1")
                .vigenciaInicio(agora.minus(30, ChronoUnit.DAYS))
                .vigenciaFim(agora.plus(60, ChronoUnit.DAYS))
                .status(EstrategiaStatus.VIGENTE)
                .criadoPor(lider.getId())
                .criadoEm(agora)
                .atualizadoEm(agora)
                .build());

        Estrategia estrategiaEncerrada = estrategiaRepository.save(Estrategia.builder()
                .titulo("Sustentabilidade na Frota")
                .descricao("Reduzir emissões e consumo de combustível da frota de veículos.")
                .categoria(Categoria.SUSTENTABILIDADE)
                .campanha("Ciclo 2025/2")
                .vigenciaInicio(agora.minus(120, ChronoUnit.DAYS))
                .vigenciaFim(agora.minus(30, ChronoUnit.DAYS))
                .status(EstrategiaStatus.ENCERRADA)
                .criadoPor(lider.getId())
                .criadoEm(agora)
                .atualizadoEm(agora)
                .build());

        ideiaRepository.save(ideia(operador.getId(), estrategiaVigente.getId(), StatusIdeia.RASCUNHO, agora, List.of()));
        ideiaRepository.save(ideia(operador.getId(), estrategiaVigente.getId(), StatusIdeia.SUBMETIDA, agora, List.of()));
        ideiaRepository.save(ideia(operador.getId(), estrategiaVigente.getId(), StatusIdeia.EM_ANALISE, agora,
                List.of(Priorizacao.builder().gestorId(gestor.getId()).nota(4).comentario("Boa ideia").em(agora).build())));
        ideiaRepository.save(ideia(operador.getId(), estrategiaVigente.getId(), StatusIdeia.APROVADA, agora,
                List.of(Priorizacao.builder().gestorId(gestor.getId()).nota(5).comentario("Aprovada").em(agora).build())));
        Ideia ideiaConvertida = ideiaRepository.save(ideia(operador.getId(), estrategiaVigente.getId(), StatusIdeia.APROVADA, agora,
                List.of(Priorizacao.builder().gestorId(gestor.getId()).nota(5).comentario("Excelente, virou projeto").em(agora).build())));
        ideiaRepository.save(ideia(operador.getId(), estrategiaVigente.getId(), StatusIdeia.REPROVADA, agora,
                List.of(Priorizacao.builder().gestorId(gestor.getId()).nota(2).comentario("Fora do escopo").em(agora).build())));

        Projeto projetoConcluido = projetoRepository.save(Projeto.builder()
                .titulo("Otimização de rotas")
                .descricao("Projeto originado da ideia aprovada de otimização logística.")
                .estrategiaId(estrategiaVigente.getId())
                .ideiaOrigemId(ideiaConvertida.getId())
                .gestorId(gestor.getId())
                .etapa(EtapaProjeto.CONCLUIDO)
                .status(StatusProjeto.ENCERRADO)
                .investimento(new BigDecimal("50000.00"))
                .prazoInicio(agora.minus(90, ChronoUnit.DAYS))
                .prazoFim(agora.minus(10, ChronoUnit.DAYS))
                .concluidoEm(agora.minus(5, ChronoUnit.DAYS))
                .percentualConcluido(100)
                .resultados(List.of(
                        resultado(agora.minus(40, ChronoUnit.DAYS), "20000.00", "5000.00", "12.0", "Resultado parcial", gestor.getId()),
                        resultado(agora.minus(10, ChronoUnit.DAYS), "30000.00", "8000.00", "15.0", "Resultado final", gestor.getId())))
                .criadoEm(agora)
                .atualizadoEm(agora)
                .build());

        ideiaConvertida.setProjetoId(projetoConcluido.getId());
        ideiaRepository.save(ideiaConvertida);

        projetoRepository.save(Projeto.builder()
                .titulo("Manutenção preditiva")
                .descricao("Sensores de manutenção preditiva na linha de produção.")
                .estrategiaId(estrategiaVigente.getId())
                .gestorId(gestor.getId())
                .etapa(EtapaProjeto.EXECUCAO)
                .status(StatusProjeto.NO_PRAZO)
                .investimento(new BigDecimal("30000.00"))
                .prazoInicio(agora.minus(30, ChronoUnit.DAYS))
                .prazoFim(agora.plus(30, ChronoUnit.DAYS))
                .percentualConcluido(40)
                .resultados(List.of(resultado(agora.minus(10, ChronoUnit.DAYS), "5000.00", "1000.00", "5.0", "Parcial", gestor.getId())))
                .criadoEm(agora)
                .atualizadoEm(agora)
                .build());

        projetoRepository.save(Projeto.builder()
                .titulo("Frota elétrica — piloto")
                .descricao("Piloto de substituição de veículos por elétricos.")
                .estrategiaId(estrategiaEncerrada.getId())
                .gestorId(gestor.getId())
                .etapa(EtapaProjeto.VALIDACAO)
                .status(StatusProjeto.ATRASADO)
                .investimento(new BigDecimal("20000.00"))
                .prazoInicio(agora.minus(60, ChronoUnit.DAYS))
                .prazoFim(agora.minus(5, ChronoUnit.DAYS))
                .percentualConcluido(80)
                .resultados(List.of())
                .criadoEm(agora)
                .atualizadoEm(agora)
                .build());

        projetoRepository.save(Projeto.builder()
                .titulo("Reciclagem de embalagens")
                .descricao("Programa de reciclagem de embalagens de transporte.")
                .estrategiaId(estrategiaEncerrada.getId())
                .gestorId(gestor.getId())
                .etapa(EtapaProjeto.PLANEJAMENTO)
                .status(StatusProjeto.PAUSADO)
                .investimento(new BigDecimal("10000.00"))
                .prazoInicio(agora)
                .prazoFim(agora.plus(90, ChronoUnit.DAYS))
                .percentualConcluido(0)
                .resultados(List.of())
                .criadoEm(agora)
                .atualizadoEm(agora)
                .build());

        log.info("Seed: 3 usuários, 2 estratégias, 6 ideias e 4 projetos carregados.");
    }

    private Usuario usuario(String nome, String email, Role role, Instant agora) {
        return Usuario.builder()
                .nome(nome)
                .email(email)
                .senhaHash(passwordEncoder.encode("senha1234"))
                .role(role)
                .ativo(true)
                .criadoEm(agora)
                .build();
    }

    private Ideia ideia(String autorId, String estrategiaId, StatusIdeia status, Instant agora, List<Priorizacao> priorizacoes) {
        BigDecimal notaMedia = priorizacoes.isEmpty()
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(priorizacoes.stream().mapToInt(Priorizacao::getNota).average().orElse(0))
                        .setScale(2, java.math.RoundingMode.HALF_UP);

        return Ideia.builder()
                .titulo("Ideia de exemplo (" + status + ")")
                .descricao("Ideia de exemplo carregada pelo seed, em estado " + status + ".")
                .autorId(autorId)
                .estrategiaId(estrategiaId)
                .status(status)
                .impacto(Nivel.ALTO)
                .esforco(Nivel.MEDIO)
                .priorizacoes(priorizacoes)
                .notaMedia(notaMedia)
                .totalVotos(priorizacoes.size())
                .criadoEm(agora)
                .atualizadoEm(agora)
                .build();
    }

    private Resultado resultado(Instant data, String receita, String economia, String ganhoPct, String observacao, String registradoPor) {
        return Resultado.builder()
                .data(data)
                .receita(new BigDecimal(receita))
                .economia(new BigDecimal(economia))
                .ganhoProdutividadePct(new BigDecimal(ganhoPct))
                .observacao(observacao)
                .registradoPor(registradoPor)
                .build();
    }
}
