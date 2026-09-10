package br.com.fiap.aguiabranca;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

// DECISION: desliga auto-index-creation só para este teste. A Etapa A
// introduziu o primeiro @Document (Usuario, com @Indexed), e sem isto o
// contexto tenta criar índices contra um Mongo real na subida — aqui não
// há Mongo local nem Atlas configurado para os testes (seção 8: "no
// apuntes los tests a Atlas"). Mongo embebido/Testcontainers é trabalho da
// Etapa H; isto é só o mínimo para não travar a Etapa A por conta disso.
@SpringBootTest
@TestPropertySource(properties = "spring.data.mongodb.auto-index-creation=false")
class AguiabrancaApplicationTests {

	@Test
	void contextLoads() {
	}

}
