package br.com.fiap.aguiabranca.ia;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ConversaIaRepository extends MongoRepository<ConversaIa, String> {

    Page<ConversaIa> findByGestorId(String gestorId, Pageable pageable);
}
