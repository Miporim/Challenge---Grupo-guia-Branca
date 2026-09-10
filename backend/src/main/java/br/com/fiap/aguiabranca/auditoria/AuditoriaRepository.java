package br.com.fiap.aguiabranca.auditoria;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuditoriaRepository extends MongoRepository<AuditoriaEvento, String> {
}
