# Roteiro de testes antes da publicação

Substitua `API` pela URL da aplicação (ex.: `http://localhost:8080`). Os exemplos usam `curl`; funcionam igual no Postman.
Pré-requisito: `script_banco.sql` executado e variáveis de ambiente configuradas (veja o README).

> Os testes de lógica de senha, sessão, permissões, limite de login e CORS foram executados com repositórios simulados em memória. **Nada aqui foi executado contra o Oracle real**: este roteiro serve justamente para isso.

## 1. Cadastro e login

| # | Teste | Comando | Esperado |
|---|---|---|---|
| 1 | Cadastro válido | `curl -X POST API/api/v1/auth/cadastro -H "Content-Type: application/json" -d '{"nome":"Teste","email":"teste@email.com","senha":"123456"}'` | 201 com `token` e `usuario` |
| 2 | E-mail repetido | repetir o comando 1 | 400 "Já existe um usuário…" |
| 3 | Senha curta | senha `123` | 400 |
| 4 | Login válido | `curl -X POST API/api/v1/auth/login -H "Content-Type: application/json" -d '{"email":"teste@email.com","senha":"123456"}'` | 200 com `token` |
| 5 | Login inválido | senha errada | 401 "E-mail ou senha incorretos." |
| 6 | Bloqueio por tentativas | 6 logins errados seguidos | 429 (mesmo com a senha certa na 6ª) |
| 7 | Conta de demonstração antiga | login `inovatech@email.com` / `123456` | 200 (e a senha é convertida para PBKDF2) |

Guarde o token: `TOKEN=...`

## 2. Sessão e rotas privadas

| # | Teste | Comando | Esperado |
|---|---|---|---|
| 8 | Sem token | `curl API/api/v1/usuarios/me` | 401 |
| 9 | Token inválido | `-H "Authorization: Bearer abc"` | 401 |
| 10 | Token válido | `-H "Authorization: Bearer $TOKEN"` | 200 com seus dados |
| 11 | Logout | `curl -X POST API/api/v1/auth/logout -H "Authorization: Bearer $TOKEN"` e depois repetir o 10 | 204, depois 401 |
| 12 | Token expirado | no Oracle: `UPDATE SESSAO SET CRIADA_EM = SYSTIMESTAMP - INTERVAL '8' DAY WHERE TOKEN = '...'; COMMIT;` e repetir o 10 | 401 |

## 3. Permissões (usuário comum)

| # | Teste | Esperado |
|---|---|---|
| 13 | `GET API/api/v1/usuarios` | 403 |
| 14 | `GET API/api/v1/usuarios/{id de OUTRO usuário}` | 403 |
| 15 | `DELETE API/api/v1/usuarios/{id de OUTRO usuário}` | 403 |
| 16 | `POST API/api/v1/niveis` (qualquer corpo) | 403 |
| 17 | `GET API/api/v1/acoes/usuario/{id de OUTRO}` | 403 |
| 18 | `POST API/api/v1/acoes` | 403 |
| 19 | `GET API/api/v1/ranking` e `GET API/api/v1/niveis` sem token | 200 (públicos) |
| 20 | Como ADMIN (veja o README) repetir 13 e 14 | 200 |

## 4. Ações, comprovantes e pontos

| # | Teste | Esperado |
|---|---|---|
| 21 | `curl -X POST API/api/v1/acoes/enviar -H "Authorization: Bearer $TOKEN" -F codigo=reciclagem-residuos -F "descricao=Separei o lixo reciclável da semana" -F arquivo=@foto.jpg` | 201, `pontosGanhos` = 100 |
| 22 | Conferir `GET /ecorank/me` | pontos e ações aumentaram; nível muda ao passar de 200/400/700/1000 |
| 23 | Enviar PDF ou `.txt` | 400 "Envie somente uma foto…" |
| 24 | Enviar arquivo `.jpg` que na verdade é outro formato | 400 "Foto inválida" |
| 25 | Foto acima de 10 MB | 400 (ou 413 se passar do limite do servidor) |
| 26 | Vídeo MP4 acima de 45 s | 400 "O vídeo deve ter no máximo 45 segundos" |
| 27 | Código de ação inexistente | 404 |
| 28 | Descrição com menos de 10 caracteres | 400 |
| 29 | Baixar o comprovante (`link` da ação, em `GET /acoes/me`) com o token do dono | 200 com o arquivo |
| 30 | Baixar o mesmo comprovante com o token de OUTRO usuário | 404 |
| 31 | Reiniciar a aplicação e repetir o 29 | 200 (o arquivo está no banco) |

## 5. Ranking, desafios, impacto

| # | Teste | Esperado |
|---|---|---|
| 32 | `GET /ranking` após o 21 | usuário sobe de posição coerente com os pontos; empate: quem se cadastrou primeiro fica na frente |
| 33 | `POST /desafios/1/participar` e enviar 3 ações de Reciclagem | `GET /desafios/me` mostra 100% e "concluído"; os pontos do desafio entram **uma única vez** |
| 34 | Repetir `POST /desafios/1/participar` | 400 "Você já participa" |
| 35 | `GET /impacto/me` | `co2Kg` = soma dos fatores das ações; `metodologia` informada |
| 36 | Conferir no front: o CO₂ do dashboard é igual ao da API | iguais |

## 6. Objetivos, contato e conteúdos

| # | Teste | Esperado |
|---|---|---|
| 37 | `POST /objetivos` com `{"id":0,"titulo":"X","descricao":"","meta":5,"progresso":0}` | 201 |
| 38 | `PUT /objetivos/{id}` com `progresso` maior que `meta` | 400 |
| 39 | `PUT`/`DELETE` em objetivo de outro usuário | 404 |
| 40 | `POST /contato` 4 vezes com o mesmo e-mail | 3 primeiras 201, a 4ª 429 |
| 41 | `GET /conteudos?categoria=Reciclagem&busca=coleta` | lista filtrada |

## 6.1 CRUD administrativo (use o token de um ADMIN)

| # | Teste | Esperado |
|---|---|---|
| 45 | `POST /acoes/catalogo` com `{"id":0,"codigo":"compostagem","rotulo":"Fiz compostagem","categoria":"Outros","pontos":120,"co2Kg":0.6}` | 201 |
| 46 | Repetir o 45 | 400 "Já existe uma ação com o código…" |
| 47 | `PUT /acoes/catalogo/{id}` trocando os pontos, depois `DELETE` | 200 e depois 204; `GET` do mesmo id devolve 404 |
| 48 | `POST /desafios` com meta 0 | 400; com meta 3 e recompensa 100: 201 |
| 49 | `POST /conteudos` com `url` sem `http` | 400; com URL válida: 201 |
| 50 | Enviar mensagem em `/contato` e depois `PUT /contato/{id}` com `{"status":"RESOLVIDO"}` | 200; status inválido: 400 |
| 51 | Repetir 45, 48, 49 e `GET /contato` com token de usuário comum | 403 |

## 7. CORS

| # | Teste | Esperado |
|---|---|---|
| 42 | `curl -i -X OPTIONS API/api/v1/ranking -H "Origin: https://site-qualquer.com" -H "Access-Control-Request-Method: GET"` | 403, sem `Access-Control-Allow-Origin` |
| 43 | Mesmo comando com `Origin: ` igual ao do front (ou `http://localhost:5173` em desenvolvimento) | 200 com `Access-Control-Allow-Origin` igual à origem |

## 8. Persistência

| # | Teste | Esperado |
|---|---|---|
| 44 | Reiniciar a aplicação e fazer login com a mesma conta | continua funcionando; pontos, ações e comprovantes mantidos |
