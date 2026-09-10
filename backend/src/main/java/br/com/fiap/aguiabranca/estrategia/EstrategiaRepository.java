package br.com.fiap.aguiabranca.estrategia;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface EstrategiaRepository extends MongoRepository<Estrategia, String> {

    List<Estrategia> findByCampanhaAndStatus(String campanha, EstrategiaStatus status);

    Optional<Estrategia> findFirstByStatusOrderByVigenciaInicioDesc(EstrategiaStatus status);
}
