package br.com.fiap.aguiabranca.ideia;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IdeiaRepository extends MongoRepository<Ideia, String> {

    Page<Ideia> findByAutorId(String autorId, Pageable pageable);

    boolean existsByEstrategiaId(String estrategiaId);

    long countByStatusNot(StatusIdeia status);

    long countByStatus(StatusIdeia status);

    long countByProjetoIdIsNotNull();

    List<Ideia> findByEstrategiaIdAndStatus(String estrategiaId, StatusIdeia status);
}
