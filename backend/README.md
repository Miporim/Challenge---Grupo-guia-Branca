# Backend — Águia Branca (Challenge FIAP)

API REST da plataforma de gestão de inovação. Java 21, Spring Boot 4.1.1,
MongoDB. A especificação completa de implementação está em
[`docs/ESPECIFICACION-BACKEND.md`](docs/ESPECIFICACION-BACKEND.md).

## Como rodar (3 comandos)

```bash
cd backend
./mvnw test
./mvnw spring-boot:run
```

1. `cd backend` — entra na subpasta do backend (o repositório também tem
   o projeto Android na raiz).
2. `./mvnw test` — compila e roda a suíte de testes (194 testes, todos
   isolados de qualquer Mongo real — ver "Testes" abaixo).
3. `./mvnw spring-boot:run` — sobe a API em `http://localhost:8080`.

Sem `MONGODB_URI` configurada, a aplicação usa
`mongodb://localhost:27017/aguiabranca` por padrão. **É preciso um
MongoDB alcançável nesse endereço (ou em `MONGODB_URI`) para a
aplicação terminar de subir** — `spring.data.mongodb.auto-index-creation:
true` cria os índices na inicialização, o que exige uma conexão real
nesse momento (não é só para persistir dados depois). Sem Mongo
disponível, a subida falha depois de ~30s esperando o driver conectar.

Depois de subir: Swagger UI em `http://localhost:8080/swagger-ui.html`,
health check público em `http://localhost:8080/actuator/health`.

## Variáveis de ambiente

| Variável | Obrigatória | Default | Uso |
|---|---|---|---|
| `MONGODB_URI` | Não (mas necessária para conectar) | `mongodb://localhost:27017/aguiabranca` | Connection string do Mongo |
| `JWT_SECRET` | Não | chave de desenvolvimento fixa | Assinatura HS256 dos tokens — **trocar em produção** |
| `SPRING_PROFILES_ACTIVE` | Não | `local` | Ver "Perfis" abaixo |
| `GEMINI_API_KEY` | Não | vazia | Sem ela, os endpoints de `/api/ia/**` respondem 503 — o resto da aplicação funciona normalmente |
| `IA_PROVIDER` | Não | `gemini` | `gemini` \| `groq` \| `ollama` — ver "IA" abaixo |
| `GROQ_API_KEY` / `OLLAMA_API_KEY` | Não | vazia | Só usadas se `IA_PROVIDER` apontar para esse provedor |

Nenhum segredo real está commitado. `backend/src/main/resources/application-local.yaml`
(se existir na sua máquina) é ignorado pelo git — é onde a connection
string do MongoDB Atlas fica, fora do repositório.

## Perfis

- **(nenhum / `local`)** — configuração padrão, conecta no Mongo de `MONGODB_URI`.
- **`seed`** — ativa `DataSeeder`, que carrega dados de exemplo (3
  usuários — um de cada role, 2 estratégias, 6 ideias, 4 projetos) numa
  base vazia. Idempotente: não duplica se já houver usuários.
  Credenciais de teste (senha `senha1234` para todos):
  `operador@aguiabranca.com`, `gestor@aguiabranca.com`, `lider@aguiabranca.com`.
  Ativar com `SPRING_PROFILES_ACTIVE=seed` (ou `local,seed`).
- **`groq`** / **`ollama`** — trocam o provedor de IA (ver "IA" abaixo).

## Arquitetura

Organização por domínio (não por camada) — cada pacote tem seu
controller, service, repository, entidade e `dto/`:

```
br.com.fiap.aguiabranca
├── config/       SecurityConfig, OpenApiConfig, AsyncConfig
├── security/     JWT, CorrelationIdFilter, rate limit de login
├── shared/       excecões e GlobalExceptionHandler (RFC 7807)
├── usuario/      autenticação, cadastro, gestão de usuários
├── estrategia/   estratégias de inovação
├── ideia/        ideias e priorização
├── projeto/      projetos e resultados
├── relatorio/    agregações MongoDB para os dashboards
├── auditoria/    trilha de auditoria por eventos
├── ia/           pontuação de ideias, chat e insights
└── seed/         DataSeeder (perfil "seed")
```

Autorização em duas camadas: regra grossa por rota em `SecurityConfig`,
regra fina por role/posse em `@PreAuthorize` nos **services** (nunca nos
controllers). Posse de recurso (autor de uma ideia, dono de uma
conversa) via beans dedicados (`IdeiaSecurity`, `ConversaSecurity`).

## IA — decisão de implementação

A especificação pede Spring AI 2.0.x, com a ressalva explícita de que,
se der problema com o Boot 4.1.1, é para implementar direto contra a
API REST do provedor. **Optei por isso desde o início**: verificar a
versão exata do Spring AI compatível com um Boot tão recente (4.1.1,
lançado pouco antes deste projeto) e validar a autoconfiguração de um
framework grande contra ele não era viável no ambiente onde este
backend foi desenvolvido — sem acesso a uma key real do Gemini para
testar de qualquer forma, o risco de perder tempo depurando
incompatibilidades de uma dependência pesada não compensava.

`GeminiClient` (provedor default) e `OpenAiCompatibleClient` (perfis
`groq`/`ollama`, que expõem a mesma API `/chat/completions`) implementam
a interface `IaClient` usando `RestClient` puro, com timeout de 10s e 1
retentativa. `IaService` não conhece qual provedor está ativo.

**Isto nunca foi testado contra uma API de IA real** — nenhuma key foi
configurada durante o desenvolvimento. Os testes automatizados mockam
`IaClient` inteiramente; o único teste que toca `GeminiClient` de
verdade (`GeminiClientTest`) prova só o caminho sem key (falha rápido,
sem chamar rede), que é justamente o critério de aceitação da etapa.

## Testes

`./mvnw test` roda 194 testes, nenhum deles precisa de MongoDB real —
todos os repositories/`MongoTemplate` são mockados. Padrão usado em
todo o projeto:

- **`*ServiceTest`** — regra de negócio pura, sem contexto Spring.
- **`*ServiceMethodSecurityTest`** — a matriz de acesso completa (seção
  8: "por cada endpoint, o rol correto passa e os outros recebem 403"),
  contra um contexto Spring mínimo com `@PreAuthorize` real, sem
  autoconfiguração do Boot.
- **`*ControllerTest`** — contrato HTTP (`@WebMvcTest`), service
  mockado — a matriz de acesso já está provada no nível de service.

### Limitação conhecida: sem verificação contra Mongo real

Nenhum teste roda contra um MongoDB de verdade. A seção 8 pede Mongo
embebido (flapdoodle) ou Testcontainers — Testcontainers está fora de
alcance nesta máquina de desenvolvimento (Docker não funciona aqui).
Cheguei a montar um teste de integração completo com Mongo embebido via
`de.flapdoodle.embed.mongo` (o mongod real chegou a subir, logs
confirmam), mas a sobrescrita da property `spring.data.mongodb.uri`
para apontar pro Mongo embebido não "venceu" o default do
`application.yaml` nesta combinação específica de Spring Boot
4.1.1/Spring Framework 7 — tentei três mecanismos (`@DynamicPropertySource`,
`System.setProperty` em bloco estático, e um `ApplicationContextInitializer`
com `TestPropertySourceUtils`, o mesmo utilitário que
`@SpringBootTest(properties = ...)` usa por baixo dos panos) e nos três
o `MongoClient` seguia tentando `localhost:27017` mesmo com a property
resolvida corretamente no `Environment` (confirmei com logs de debug).
Isolei que a sobrescrita de property **em si** funciona nesta versão
(um teste minimalista com `@SpringBootTest(properties = ...)` puro
funciona), então o problema é específico da combinação com um valor
calculado em runtime + `@ActiveProfiles`. Não persegui mais — reverti o
teste e a dependência do flapdoodle para não deixar código quebrado no
repositório.

**Consequência prática:** os pipelines de agregação do
`RelatorioService` (Etapa E) e a persistência real via `DataSeeder`
nunca foram exercitados contra um Mongo de verdade neste ambiente — só
contra mocks. Quem for rodar isto com Docker funcionando ou acesso ao
MongoDB Atlas devia, como primeira validação, subir a aplicação com
`SPRING_PROFILES_ACTIVE=seed` e chamar `GET /api/relatorios/resumo`
autenticado como líder, conferindo que os números batem com os 4
projetos de exemplo do seed.
