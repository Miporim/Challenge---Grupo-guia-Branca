package br.com.fiap.aguiabranca.projeto;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProjetoRepository extends MongoRepository<Projeto, String> {

    boolean existsByEstrategiaId(String estrategiaId);

    long countByIdeiaOrigemIdIsNotNullAndEtapa(EtapaProjeto etapa);
}
