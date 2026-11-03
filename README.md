# Grupo Inovatech

| Integrante | RM |
|---|---|
| Guilherme Joel de Camargo | RM570403 |
| Pedro Felipe de Castro Rosa | RM569049 |
| Arthur Chang Skolimoski | RM572510 |
| Victor Ferreira Gomes | RM569273 |
| Rafaela Donadio Figueiredo Tavares | RM572797 |
| Pedro Henrique Cardoso Cavalcante de Souza | RM570464 |

---
Link De Base, Use ele: https://github.com/Pedrin740/Back-end-Challenge

# EcoRank – API Restful (Spring Boot)

API de ranking de ações sustentáveis. Projeto da entrega final de Domain Driven Design using Java (FIAP).

## Tecnologias
Java 21, Spring Boot 4.1.0, Maven, JDBC (DataSource), Oracle, Docker, Render.

## Estrutura (pacote `br.com.ecorank.ecorank`)
- `model` – entidades (Usuario, AcaoSustentavel, Missao, Conquista, Nivel, Status)
- `repository` – DAO com JDBC/DataSource
- `service` – BO: regras de negócio e validações
- `resource` – endpoints REST (`@RestController`)
- `dto` / `mapper` – records DTO e conversão DTO ↔ entidade
- `exception` – exceções do sistema
- `config` – `CorsFilter` (CORS restrito às origens autorizadas)

## Configuração (variáveis de ambiente)
O projeto **não guarda senha nem usuário do banco em arquivo**. Sem as variáveis abaixo a aplicação não inicia (de propósito).

| Variável | Obrigatória | Exemplo |
|---|---|---|
| `DB_URL` | sim | `jdbc:oracle:thin:@HOST:1521:SID` |
| `DB_USERNAME` | sim | seu usuário do Oracle |
| `DB_PASSWORD` | sim | sua senha do Oracle |
| `CORS_ORIGINS` | em produção | `https://meu-front.vercel.app` (vários: separados por vírgula) |
| `PORT` | não | o Render define sozinho (padrão 8080) |

Veja o modelo em `.env.example`. No IntelliJ: *Run > Edit Configurations > Environment variables*. No PowerShell:
```
$env:DB_URL="jdbc:oracle:thin:@HOST:1521:SID"; $env:DB_USERNAME="usuario"; $env:DB_PASSWORD="senha"
```

## Como rodar
1. Execute `script_banco.sql` no Oracle (SQL Developer, arquivo em UTF-8).
2. Defina as variáveis de ambiente acima.
3. Rode a classe `EcorankApplication` (IntelliJ) ou:
   ```
   mvn clean package -DskipTests
   java -jar target/ecorank-0.0.1-SNAPSHOT.jar
   ```
4. Teste: `http://localhost:8080/api/v1/ranking` , Link de Base: https://github.com/Pedrin740/Back-end-Challenge

## Endpoints
`/api/v1/usuarios`, `/api/v1/acoes`, `/api/v1/missoes`, `/api/v1/conquistas`, `/api/v1/niveis` (GET, GET /{id}, POST, PUT /{id}, DELETE /{id};
acoes, missoes e conquistas também têm GET /usuario/{idUsuario}) e `GET /api/v1/ranking`.
A tabela completa está na documentação PDF.

> **Atenção:** desde a revisão de segurança, esses endpoints antigos exigem login. Veja a seção *Permissões* abaixo.

## Endpoints da Sprint 04 (consumidos pelo front-end React)

Rotas protegidas exigem o header `Authorization: Bearer <token>` (o token vem do login/cadastro).

| Método | Endpoint | Login | O que faz |
|---|---|---|---|
| POST | `/api/v1/auth/cadastro` | não | Cria a conta (senha com PBKDF2 + salt) e devolve `{token, usuario}` |
| POST | `/api/v1/auth/login` | não | Autentica e devolve `{token, usuario}` |
| POST | `/api/v1/auth/logout` | sim | Encerra a sessão |
| GET / PUT | `/api/v1/usuarios/me` | sim | Consulta / atualiza o próprio perfil |
| GET | `/api/v1/ecorank/me` | sim | Resumo do dashboard: pontos, ações, nível, próximo nível, posição, semana, CO₂, categorias |
| GET | `/api/v1/impacto/me` | sim | Evolução de pontos (30 dias), CO₂ estimado e metodologia |
| GET | `/api/v1/ranking` | não | Ranking geral (empate: quem se cadastrou primeiro) |
| GET | `/api/v1/acoes/catalogo` | não | Catálogo oficial de ações (rótulo, categoria, pontos) |
| POST | `/api/v1/acoes/enviar` | sim | `multipart/form-data` com `codigo`, `descricao`, `arquivo`. Valida, registra, calcula pontos e atualiza nível/desafios |
| GET | `/api/v1/acoes/me` | sim | Histórico de ações do usuário |
| GET | `/api/v1/comprovacoes/{arquivo}` | sim | Baixa a foto/vídeo (somente o dono da ação) |
| GET | `/api/v1/desafios` | não | Catálogo de desafios |
| GET | `/api/v1/desafios/me` | sim | Desafios com o progresso do usuário |
| POST | `/api/v1/desafios/{id}/participar` | sim | Entra em um desafio |
| GET, POST | `/api/v1/objetivos` | sim | Lista / cria objetivos pessoais |
| PUT, DELETE | `/api/v1/objetivos/{id}` | sim | Atualiza (inclui progresso) / exclui |
| POST | `/api/v1/contato` | não | Grava a mensagem do "Fale conosco" |
| GET | `/api/v1/conteudos?categoria=&busca=` | não | Notícias ambientais |

### Regras principais (todas no servidor)
- **Pontos**: vêm da tabela `CATALOGO_ACAO`; o navegador só envia o código da ação.
- **Comprovação**: foto JPG/PNG/WEBP/GIF até 10 MB, ou vídeo MP4/MOV até 25 MB e **45 segundos** (duração lida do próprio arquivo). O arquivo é gravado **no próprio Oracle (coluna BLOB)**, na mesma transação da ação, então não se perde quando a hospedagem reinicia.
- **Níveis**: tabela `NIVEL` (Bronze 0, Prata 200, Ouro 400, Diamante 700, Esmeralda 1000).
- **Desafios**: concluído quando o usuário faz `META_ACOES` ações aprovadas (da categoria do desafio, ou qualquer uma) depois de participar; a recompensa é somada uma única vez.
- **CO₂**: estimativa por tipo de ação (`CATALOGO_ACAO.CO2_KG`), não é medição real.

### CRUD administrativo (somente ADMIN para escrever)
| Recurso | Endpoints |
|---|---|
| Catálogo de ações | `GET /acoes/catalogo/{id}` (público), `POST /acoes/catalogo`, `PUT` e `DELETE /acoes/catalogo/{id}` |
| Desafios | `GET /desafios/{id}` (público), `POST /desafios`, `PUT` e `DELETE /desafios/{id}` |
| Conteúdos | `GET /conteudos/{id}` (público), `POST /conteudos`, `PUT` e `DELETE /conteudos/{id}` |
| Mensagens de contato | `GET /contato`, `GET /contato/{id}`, `PUT /contato/{id}` (campo `status`: NOVO, EM_ANDAMENTO ou RESOLVIDO), `DELETE /contato/{id}` |

A tabela completa (65 endpoints, com verbo, acesso e códigos de status) está na documentação em `docs/EcoRank_Documentacao.pdf`.

### Permissões (quem pode o quê)
Rotas "com login" exigem `Authorization: Bearer <token>`. Respostas: **401** sem login/token inválido ou expirado, **403** sem permissão, **429** limite de tentativas.

| Recurso | Público | Dono dos dados | Somente ADMIN |
|---|---|---|---|
| `/ranking`, `/niveis` (GET), `/acoes/catalogo`, `/desafios`, `/conteudos`, `POST /contato` | ✔ | | |
| `/usuarios/me`, `/ecorank/me`, `/impacto/me`, `/acoes/me`, `/acoes/enviar`, `/desafios/me`, `/objetivos`, `/comprovacoes/{arquivo}` | | ✔ (sempre o próprio usuário logado) | |
| `GET/PUT/DELETE /usuarios/{id}`, `GET /acoes/{id}`, `/acoes/usuario/{id}`, `/missoes/usuario/{id}`, `/conquistas/usuario/{id}` e `GET /{id}` de missões/conquistas | | ✔ (ou ADMIN) | ✔ |
| `GET /usuarios`, `POST /usuarios`, `GET /acoes`, `POST/PUT /acoes`, `GET /missoes`, `GET /conquistas` | | | ✔ |
| `POST/PUT/DELETE` de `/niveis`, `/missoes`, `/conquistas`, `/desafios`, `/conteudos`, `/acoes/catalogo`; todo o `/contato` exceto o `POST` | | | ✔ |

- `POST /acoes` e `PUT /acoes/{id}` (envio só com link) agora são administrativos: um link não prova a ação. Usuários comuns enviam ações por `POST /acoes/enviar`, com foto/vídeo.
- **Como criar um ADMIN:** cadastre a conta pelo site e rode no Oracle `UPDATE USUARIO SET PERFIL = 'ADMIN' WHERE EMAIL = 'seu@email.com'; COMMIT;`. Não existe admin com senha padrão.

### Segurança implementada
- **Senhas:** PBKDF2-HMAC-SHA256 (310.000 iterações, salt aleatório), no formato `pbkdf2$iterações$hash`. Contas antigas (SHA-256 simples) continuam entrando e são convertidas no primeiro login.
- **Sessão:** token aleatório de 256 bits (`SecureRandom`) na tabela `SESSAO`, validade de 7 dias, apagado no logout; sessões vencidas são limpas a cada login.
- **Login:** após 5 senhas erradas para o mesmo e-mail, bloqueio de 15 minutos (HTTP 429). O contador fica na memória e zera se a aplicação reiniciar.
- **Contato:** máximo de 3 mensagens por hora por e-mail e 200 por hora no total (HTTP 429).
- **CORS:** só as origens de `CORS_ORIGINS` (em produção) ou `localhost` (desenvolvimento). `*` não é aceito.
- **Credenciais:** nenhuma senha no código; use as variáveis de ambiente.

### Impacto ambiental (CO₂)
É uma **estimativa**, não uma medição. Cada ação do catálogo tem um fator fixo em `CATALOGO_ACAO.CO2_KG` (kg de CO₂ evitados por ação). O valor é gravado na ação no momento do envio (`ACAO_SUSTENTAVEL.CO2_KG`) e o total soma só as ações aprovadas. Ações cadastradas por outros caminhos, sem fator, contam 0 kg.

| Ação (catálogo) | Pontos | kg CO₂ |
|---|---|---|
| Separar resíduos | 100 | 0,40 |
| Reutilizar materiais | 150 | 0,80 |
| Bicicleta / caminhada | 150 | 2,00 |
| Transporte público | 100 | 1,50 |
| Economizar água | 80 | 0,30 |
| Banho consciente | 60 | 0,20 |
| Economizar energia | 90 | 0,90 |
| Desligar eletrônicos | 70 | 0,40 |
| Plantio / área verde | 200 | 5,00 |
| Alternativa às sacolas | 100 | 0,10 |

Os fatores são valores de partida definidos pela equipe de desenvolvimento e **precisam ser validados** com uma fonte antes de serem apresentados como dado real.

### Banco de dados
Rode o `script_banco.sql` inteiro (ele recria todas as tabelas e insere dados de demonstração).
Usuários de teste (senha `123456`): `inovatech@email.com`, `anaclara@email.com`, `pedrohenrique@email.com`, `mariana@email.com`, `joao@email.com`.

> O script precisa ser executado em UTF-8 (há acentos nos textos).

## Deploy no Render
Web Service > Language **Docker** > Dockerfile Path `./Dockerfile` > variáveis `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` e `CORS_ORIGINS` (endereço do front publicado).

## Documentação
`docs/EcoRank_Documentacao.pdf` (e o `.docx` editável): capa, objetivo, funcionalidades, tabela de endpoints, exemplos, prints das telas, MER, diagrama de classes e procedimentos de execução. Os diagramas (`docs/*.png`) foram gerados a partir do código e do `script_banco.sql`.


