-- =====================================================
-- EcoRank - Script de criacao do banco de dados (Oracle)
-- Se as tabelas ainda nao existirem, os DROP abaixo vao
-- mostrar erro ORA-00942, e isso pode ser ignorado.
-- =====================================================

DROP TABLE SESSAO CASCADE CONSTRAINTS;
DROP TABLE COMPROVACAO CASCADE CONSTRAINTS;
DROP TABLE PARTICIPACAO_DESAFIO CASCADE CONSTRAINTS;
DROP TABLE DESAFIO CASCADE CONSTRAINTS;
DROP TABLE OBJETIVO CASCADE CONSTRAINTS;
DROP TABLE CONTATO CASCADE CONSTRAINTS;
DROP TABLE CONTEUDO CASCADE CONSTRAINTS;
DROP TABLE CATALOGO_ACAO CASCADE CONSTRAINTS;
DROP TABLE CONQUISTA CASCADE CONSTRAINTS;
DROP TABLE MISSAO CASCADE CONSTRAINTS;
DROP TABLE ACAO_SUSTENTAVEL CASCADE CONSTRAINTS;
DROP TABLE NIVEL CASCADE CONSTRAINTS;
DROP TABLE USUARIO CASCADE CONSTRAINTS;

CREATE TABLE USUARIO (
    ID          NUMBER GENERATED ALWAYS AS IDENTITY,
    NOME        VARCHAR2(100) NOT NULL,
    EMAIL       VARCHAR2(100) NOT NULL,
    PONTOS      NUMBER(10)    DEFAULT 0 NOT NULL,
    NIVEL       VARCHAR2(30)  DEFAULT 'Bronze' NOT NULL,
    RECOMPENSA  VARCHAR2(250),
    SENHA_HASH  VARCHAR2(120),   -- pbkdf2$iteracoes$hash (ou SHA-256 antigo, convertido no 1o login)
    SALT        VARCHAR2(40),
    PERFIL      VARCHAR2(10)  DEFAULT 'USUARIO' NOT NULL,
    CONSTRAINT CK_USUARIO_PERFIL CHECK (PERFIL IN ('USUARIO', 'ADMIN')),
    CONSTRAINT PK_USUARIO PRIMARY KEY (ID),
    CONSTRAINT UK_USUARIO_EMAIL UNIQUE (EMAIL)
);

CREATE TABLE NIVEL (
    ID                  NUMBER GENERATED ALWAYS AS IDENTITY,
    NOME                VARCHAR2(30) NOT NULL,
    PONTOS_NECESSARIOS  NUMBER(10)   NOT NULL,
    MULTIPLICADOR       NUMBER(4,2)  NOT NULL,
    CONSTRAINT PK_NIVEL PRIMARY KEY (ID)
);

CREATE TABLE ACAO_SUSTENTAVEL (
    ID              NUMBER GENERATED ALWAYS AS IDENTITY,
    ID_USUARIO      NUMBER         NOT NULL,
    CATEGORIA       VARCHAR2(50)   NOT NULL,
    DESCRICAO       VARCHAR2(300)  NOT NULL,
    LINK_EVIDENCIA  VARCHAR2(500),
    STATUS          VARCHAR2(20)   NOT NULL,
    PONTOS          NUMBER(10)     DEFAULT 0 NOT NULL,
    DATA_ACAO       DATE           NOT NULL,
    CO2_KG          NUMBER(8,2)    DEFAULT 0 NOT NULL,
    CONSTRAINT PK_ACAO_SUSTENTAVEL PRIMARY KEY (ID),
    CONSTRAINT FK_ACAO_USUARIO FOREIGN KEY (ID_USUARIO)
        REFERENCES USUARIO (ID) ON DELETE CASCADE,
    CONSTRAINT CK_ACAO_STATUS CHECK
        (STATUS IN ('PENDENTE', 'APROVADA', 'REJEITADA', 'SINALIZADA'))
);

CREATE TABLE CONQUISTA (
    ID              NUMBER GENERATED ALWAYS AS IDENTITY,
    ID_USUARIO      NUMBER         NOT NULL,
    NOME            VARCHAR2(100)  NOT NULL,
    DESCRICAO       VARCHAR2(300),
    DATA_CONQUISTA  DATE           NOT NULL,
    CONSTRAINT PK_CONQUISTA PRIMARY KEY (ID),
    CONSTRAINT FK_CONQUISTA_USUARIO FOREIGN KEY (ID_USUARIO)
        REFERENCES USUARIO (ID) ON DELETE CASCADE
);

CREATE TABLE MISSAO (
    ID          NUMBER GENERATED ALWAYS AS IDENTITY,
    ID_USUARIO  NUMBER         NOT NULL,
    DESCRICAO   VARCHAR2(200)  NOT NULL,
    OBJETIVO    NUMBER(5)      NOT NULL,
    PROGRESSO   NUMBER(5)      DEFAULT 0 NOT NULL,
    RECOMPENSA  NUMBER(10)     NOT NULL,
    PRAZO       DATE           NOT NULL,
    CONSTRAINT PK_MISSAO PRIMARY KEY (ID),
    CONSTRAINT FK_MISSAO_USUARIO FOREIGN KEY (ID_USUARIO)
        REFERENCES USUARIO (ID) ON DELETE CASCADE
);


-- ---------------- Sprint 04: tabelas usadas pelo front-end ----------------

-- Sessao de login: o token e enviado pelo front no header Authorization
CREATE TABLE SESSAO (
    TOKEN       VARCHAR2(64) NOT NULL,
    ID_USUARIO  NUMBER       NOT NULL,
    CRIADA_EM   TIMESTAMP    DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT PK_SESSAO PRIMARY KEY (TOKEN),
    CONSTRAINT FK_SESSAO_USUARIO FOREIGN KEY (ID_USUARIO)
        REFERENCES USUARIO (ID) ON DELETE CASCADE
);

-- Catalogo oficial de acoes: pontos e CO2 estimado ficam no servidor.
-- CO2_KG e uma ESTIMATIVA simples por acao (valores a validar pela equipe).
CREATE TABLE CATALOGO_ACAO (
    ID         NUMBER GENERATED ALWAYS AS IDENTITY,
    CODIGO     VARCHAR2(50)  NOT NULL,
    ROTULO     VARCHAR2(150) NOT NULL,
    CATEGORIA  VARCHAR2(50)  NOT NULL,
    PONTOS     NUMBER(10)    NOT NULL,
    CO2_KG     NUMBER(8,2)   DEFAULT 0 NOT NULL,
    CONSTRAINT PK_CATALOGO_ACAO PRIMARY KEY (ID),
    CONSTRAINT UK_CATALOGO_CODIGO UNIQUE (CODIGO)
);

CREATE TABLE COMPROVACAO (
    ID             NUMBER GENERATED ALWAYS AS IDENTITY,
    ID_ACAO        NUMBER        NOT NULL,
    NOME_ORIGINAL  VARCHAR2(200) NOT NULL,
    TIPO           VARCHAR2(100) NOT NULL,
    TAMANHO        NUMBER(12)    NOT NULL,
    ARQUIVO        VARCHAR2(100) NOT NULL,
    DURACAO_SEG    NUMBER(6),
    CONTEUDO       BLOB          NOT NULL,   -- a foto/video fica no proprio banco (persistente)
    CONSTRAINT PK_COMPROVACAO PRIMARY KEY (ID),
    CONSTRAINT UK_COMPROVACAO_ARQUIVO UNIQUE (ARQUIVO),
    CONSTRAINT FK_COMPROVACAO_ACAO FOREIGN KEY (ID_ACAO)
        REFERENCES ACAO_SUSTENTAVEL (ID) ON DELETE CASCADE
);

-- Desafio: concluido quando o usuario tem META_ACOES acoes aprovadas
-- (da CATEGORIA, ou de qualquer uma se NULL) depois que comecou a participar.
CREATE TABLE DESAFIO (
    ID          NUMBER GENERATED ALWAYS AS IDENTITY,
    TITULO      VARCHAR2(100) NOT NULL,
    DESCRICAO   VARCHAR2(300) NOT NULL,
    RECOMPENSA  NUMBER(10)    NOT NULL,
    CATEGORIA   VARCHAR2(50),
    META_ACOES  NUMBER(5)     NOT NULL,
    ATIVO       NUMBER(1)     DEFAULT 1 NOT NULL,
    CONSTRAINT PK_DESAFIO PRIMARY KEY (ID)
);

CREATE TABLE PARTICIPACAO_DESAFIO (
    ID              NUMBER GENERATED ALWAYS AS IDENTITY,
    ID_USUARIO      NUMBER  NOT NULL,
    ID_DESAFIO      NUMBER  NOT NULL,
    DATA_INICIO     DATE    NOT NULL,
    CONCLUIDO       NUMBER(1) DEFAULT 0 NOT NULL,
    DATA_CONCLUSAO  DATE,
    CONSTRAINT PK_PARTICIPACAO PRIMARY KEY (ID),
    CONSTRAINT UK_PARTICIPACAO UNIQUE (ID_USUARIO, ID_DESAFIO),
    CONSTRAINT FK_PART_USUARIO FOREIGN KEY (ID_USUARIO)
        REFERENCES USUARIO (ID) ON DELETE CASCADE,
    CONSTRAINT FK_PART_DESAFIO FOREIGN KEY (ID_DESAFIO)
        REFERENCES DESAFIO (ID) ON DELETE CASCADE
);

-- Objetivos pessoais (progresso atualizado pelo proprio usuario)
CREATE TABLE OBJETIVO (
    ID          NUMBER GENERATED ALWAYS AS IDENTITY,
    ID_USUARIO  NUMBER        NOT NULL,
    TITULO      VARCHAR2(100) NOT NULL,
    DESCRICAO   VARCHAR2(300),
    META        NUMBER(5)     NOT NULL,
    PROGRESSO   NUMBER(5)     DEFAULT 0 NOT NULL,
    CONSTRAINT PK_OBJETIVO PRIMARY KEY (ID),
    CONSTRAINT FK_OBJETIVO_USUARIO FOREIGN KEY (ID_USUARIO)
        REFERENCES USUARIO (ID) ON DELETE CASCADE
);

CREATE TABLE CONTATO (
    ID          NUMBER GENERATED ALWAYS AS IDENTITY,
    NOME        VARCHAR2(100)  NOT NULL,
    EMAIL       VARCHAR2(100)  NOT NULL,
    MOTIVO      VARCHAR2(30)   NOT NULL,
    MENSAGEM    VARCHAR2(2000) NOT NULL,
    STATUS      VARCHAR2(20)   DEFAULT 'NOVO' NOT NULL,
    DATA_ENVIO  DATE           DEFAULT SYSDATE NOT NULL,
    CONSTRAINT PK_CONTATO PRIMARY KEY (ID)
);

CREATE TABLE CONTEUDO (
    ID               NUMBER GENERATED ALWAYS AS IDENTITY,
    CATEGORIA        VARCHAR2(50)  NOT NULL,
    TITULO           VARCHAR2(250) NOT NULL,
    RESUMO           VARCHAR2(500) NOT NULL,
    FONTE            VARCHAR2(100) NOT NULL,
    DATA_PUBLICACAO  DATE          NOT NULL,
    URL              VARCHAR2(500) NOT NULL,
    CONSTRAINT PK_CONTEUDO PRIMARY KEY (ID)
);

-- Niveis iniciais
INSERT INTO NIVEL (NOME, PONTOS_NECESSARIOS, MULTIPLICADOR) VALUES ('Bronze', 0, 1.00);
INSERT INTO NIVEL (NOME, PONTOS_NECESSARIOS, MULTIPLICADOR) VALUES ('Prata', 200, 1.10);
INSERT INTO NIVEL (NOME, PONTOS_NECESSARIOS, MULTIPLICADOR) VALUES ('Ouro', 400, 1.20);
INSERT INTO NIVEL (NOME, PONTOS_NECESSARIOS, MULTIPLICADOR) VALUES ('Diamante', 700, 1.50);
INSERT INTO NIVEL (NOME, PONTOS_NECESSARIOS, MULTIPLICADOR) VALUES ('Esmeralda', 1000, 2.00);

-- Catalogo de acoes (mesmos valores que antes estavam no front)
INSERT INTO CATALOGO_ACAO (CODIGO, ROTULO, CATEGORIA, PONTOS, CO2_KG) VALUES ('reciclagem-residuos', 'Separei corretamente meus resíduos', 'Reciclagem', 100, 0.40);
INSERT INTO CATALOGO_ACAO (CODIGO, ROTULO, CATEGORIA, PONTOS, CO2_KG) VALUES ('reutilizacao-materiais', 'Reutilizei materiais que seriam descartados', 'Reciclagem', 150, 0.80);
INSERT INTO CATALOGO_ACAO (CODIGO, ROTULO, CATEGORIA, PONTOS, CO2_KG) VALUES ('bicicleta-caminhada', 'Usei bicicleta ou caminhei como transporte', 'Mobilidade', 150, 2.00);
INSERT INTO CATALOGO_ACAO (CODIGO, ROTULO, CATEGORIA, PONTOS, CO2_KG) VALUES ('transporte-publico', 'Utilizei transporte público', 'Mobilidade', 100, 1.50);
INSERT INTO CATALOGO_ACAO (CODIGO, ROTULO, CATEGORIA, PONTOS, CO2_KG) VALUES ('economia-agua', 'Adotei uma prática para economizar água', 'Economia de água', 80, 0.30);
INSERT INTO CATALOGO_ACAO (CODIGO, ROTULO, CATEGORIA, PONTOS, CO2_KG) VALUES ('banho-consciente', 'Reduzi o tempo do meu banho', 'Economia de água', 60, 0.20);
INSERT INTO CATALOGO_ACAO (CODIGO, ROTULO, CATEGORIA, PONTOS, CO2_KG) VALUES ('economia-energia', 'Reduzi o consumo de energia', 'Energia limpa', 90, 0.90);
INSERT INTO CATALOGO_ACAO (CODIGO, ROTULO, CATEGORIA, PONTOS, CO2_KG) VALUES ('desligar-eletronicos', 'Desliguei aparelhos que não estavam em uso', 'Energia limpa', 70, 0.40);
INSERT INTO CATALOGO_ACAO (CODIGO, ROTULO, CATEGORIA, PONTOS, CO2_KG) VALUES ('plantio', 'Plantei ou cuidei de uma área verde', 'Outros', 200, 5.00);
INSERT INTO CATALOGO_ACAO (CODIGO, ROTULO, CATEGORIA, PONTOS, CO2_KG) VALUES ('sacolas-reutilizaveis', 'Utilizei uma alternativa às sacolas descartáveis', 'Outros', 100, 0.10);

-- Desafios
INSERT INTO DESAFIO (TITULO, DESCRICAO, RECOMPENSA, CATEGORIA, META_ACOES) VALUES ('Reduza, Reutilize, Transforme!', 'Mostre como você reutiliza materiais no seu dia a dia. Registre 3 ações de reciclagem.', 200, 'Reciclagem', 3);
INSERT INTO DESAFIO (TITULO, DESCRICAO, RECOMPENSA, CATEGORIA, META_ACOES) VALUES ('Semana sem desperdício', 'Registre 5 atitudes sustentáveis que reduzam o desperdício.', 150, NULL, 5);
INSERT INTO DESAFIO (TITULO, DESCRICAO, RECOMPENSA, CATEGORIA, META_ACOES) VALUES ('Mobilidade consciente', 'Compartilhe 3 alternativas sustentáveis de transporte.', 150, 'Mobilidade', 3);

-- Usuarios de demonstracao (senha de todos: 123456)
-- SENHA_HASH = SHA-256 em hexadecimal de (SALT || senha), igual ao calculado pelo Java
INSERT INTO USUARIO (NOME, EMAIL, PONTOS, NIVEL, SENHA_HASH, SALT) VALUES ('InovaTech', 'inovatech@email.com', 250, 'Prata', RAWTOHEX(STANDARD_HASH('demo' || '123456', 'SHA256')), 'demo');
INSERT INTO USUARIO (NOME, EMAIL, PONTOS, NIVEL, SENHA_HASH, SALT) VALUES ('Ana Clara', 'anaclara@email.com', 520, 'Ouro', RAWTOHEX(STANDARD_HASH('demo' || '123456', 'SHA256')), 'demo');
INSERT INTO USUARIO (NOME, EMAIL, PONTOS, NIVEL, SENHA_HASH, SALT) VALUES ('Pedro Henrique', 'pedrohenrique@email.com', 340, 'Prata', RAWTOHEX(STANDARD_HASH('demo' || '123456', 'SHA256')), 'demo');
INSERT INTO USUARIO (NOME, EMAIL, PONTOS, NIVEL, SENHA_HASH, SALT) VALUES ('Mariana Costa', 'mariana@email.com', 180, 'Bronze', RAWTOHEX(STANDARD_HASH('demo' || '123456', 'SHA256')), 'demo');
INSERT INTO USUARIO (NOME, EMAIL, PONTOS, NIVEL, SENHA_HASH, SALT) VALUES ('João Victor', 'joao@email.com', 120, 'Bronze', RAWTOHEX(STANDARD_HASH('demo' || '123456', 'SHA256')), 'demo');

-- Acoes aprovadas dos usuarios de demonstracao (somam exatamente os pontos acima)
INSERT INTO ACAO_SUSTENTAVEL (ID_USUARIO, CATEGORIA, DESCRICAO, LINK_EVIDENCIA, STATUS, PONTOS, DATA_ACAO, CO2_KG) SELECT ID, 'Reciclagem', 'Separei corretamente meus resíduos', NULL, 'APROVADA', 100, SYSDATE - 5, 0.40 FROM USUARIO WHERE EMAIL = 'inovatech@email.com';
INSERT INTO ACAO_SUSTENTAVEL (ID_USUARIO, CATEGORIA, DESCRICAO, LINK_EVIDENCIA, STATUS, PONTOS, DATA_ACAO, CO2_KG) SELECT ID, 'Mobilidade', 'Usei bicicleta ou caminhei como transporte', NULL, 'APROVADA', 150, SYSDATE - 2, 2.00 FROM USUARIO WHERE EMAIL = 'inovatech@email.com';
INSERT INTO ACAO_SUSTENTAVEL (ID_USUARIO, CATEGORIA, DESCRICAO, LINK_EVIDENCIA, STATUS, PONTOS, DATA_ACAO, CO2_KG) SELECT ID, 'Reciclagem', 'Reutilizei materiais que seriam descartados', NULL, 'APROVADA', 150, SYSDATE - 9, 0.80 FROM USUARIO WHERE EMAIL = 'anaclara@email.com';
INSERT INTO ACAO_SUSTENTAVEL (ID_USUARIO, CATEGORIA, DESCRICAO, LINK_EVIDENCIA, STATUS, PONTOS, DATA_ACAO, CO2_KG) SELECT ID, 'Mobilidade', 'Utilizei transporte público', NULL, 'APROVADA', 100, SYSDATE - 6, 1.50 FROM USUARIO WHERE EMAIL = 'anaclara@email.com';
INSERT INTO ACAO_SUSTENTAVEL (ID_USUARIO, CATEGORIA, DESCRICAO, LINK_EVIDENCIA, STATUS, PONTOS, DATA_ACAO, CO2_KG) SELECT ID, 'Economia de água', 'Adotei uma prática para economizar água', NULL, 'APROVADA', 80, SYSDATE - 4, 0.30 FROM USUARIO WHERE EMAIL = 'anaclara@email.com';
INSERT INTO ACAO_SUSTENTAVEL (ID_USUARIO, CATEGORIA, DESCRICAO, LINK_EVIDENCIA, STATUS, PONTOS, DATA_ACAO, CO2_KG) SELECT ID, 'Energia limpa', 'Reduzi o consumo de energia', NULL, 'APROVADA', 90, SYSDATE - 2, 0.90 FROM USUARIO WHERE EMAIL = 'anaclara@email.com';
INSERT INTO ACAO_SUSTENTAVEL (ID_USUARIO, CATEGORIA, DESCRICAO, LINK_EVIDENCIA, STATUS, PONTOS, DATA_ACAO, CO2_KG) SELECT ID, 'Outros', 'Utilizei uma alternativa às sacolas descartáveis', NULL, 'APROVADA', 100, SYSDATE - 1, 0.10 FROM USUARIO WHERE EMAIL = 'anaclara@email.com';
INSERT INTO ACAO_SUSTENTAVEL (ID_USUARIO, CATEGORIA, DESCRICAO, LINK_EVIDENCIA, STATUS, PONTOS, DATA_ACAO, CO2_KG) SELECT ID, 'Reciclagem', 'Separei corretamente meus resíduos', NULL, 'APROVADA', 100, SYSDATE - 8, 0.40 FROM USUARIO WHERE EMAIL = 'pedrohenrique@email.com';
INSERT INTO ACAO_SUSTENTAVEL (ID_USUARIO, CATEGORIA, DESCRICAO, LINK_EVIDENCIA, STATUS, PONTOS, DATA_ACAO, CO2_KG) SELECT ID, 'Mobilidade', 'Usei bicicleta ou caminhei como transporte', NULL, 'APROVADA', 150, SYSDATE - 3, 2.00 FROM USUARIO WHERE EMAIL = 'pedrohenrique@email.com';
INSERT INTO ACAO_SUSTENTAVEL (ID_USUARIO, CATEGORIA, DESCRICAO, LINK_EVIDENCIA, STATUS, PONTOS, DATA_ACAO, CO2_KG) SELECT ID, 'Energia limpa', 'Reduzi o consumo de energia', NULL, 'APROVADA', 90, SYSDATE - 1, 0.90 FROM USUARIO WHERE EMAIL = 'pedrohenrique@email.com';
INSERT INTO ACAO_SUSTENTAVEL (ID_USUARIO, CATEGORIA, DESCRICAO, LINK_EVIDENCIA, STATUS, PONTOS, DATA_ACAO, CO2_KG) SELECT ID, 'Reciclagem', 'Separei corretamente meus resíduos', NULL, 'APROVADA', 100, SYSDATE - 7, 0.40 FROM USUARIO WHERE EMAIL = 'mariana@email.com';
INSERT INTO ACAO_SUSTENTAVEL (ID_USUARIO, CATEGORIA, DESCRICAO, LINK_EVIDENCIA, STATUS, PONTOS, DATA_ACAO, CO2_KG) SELECT ID, 'Economia de água', 'Adotei uma prática para economizar água', NULL, 'APROVADA', 80, SYSDATE - 2, 0.30 FROM USUARIO WHERE EMAIL = 'mariana@email.com';
INSERT INTO ACAO_SUSTENTAVEL (ID_USUARIO, CATEGORIA, DESCRICAO, LINK_EVIDENCIA, STATUS, PONTOS, DATA_ACAO, CO2_KG) SELECT ID, 'Economia de água', 'Reduzi o tempo do meu banho', NULL, 'APROVADA', 60, SYSDATE - 4, 0.20 FROM USUARIO WHERE EMAIL = 'joao@email.com';
INSERT INTO ACAO_SUSTENTAVEL (ID_USUARIO, CATEGORIA, DESCRICAO, LINK_EVIDENCIA, STATUS, PONTOS, DATA_ACAO, CO2_KG) SELECT ID, 'Economia de água', 'Reduzi o tempo do meu banho', NULL, 'APROVADA', 60, SYSDATE - 1, 0.20 FROM USUARIO WHERE EMAIL = 'joao@email.com';

-- Objetivos pessoais dos usuarios de demonstracao
INSERT INTO OBJETIVO (ID_USUARIO, TITULO, DESCRICAO, META, PROGRESSO) SELECT ID, 'Reciclar durante o mês', 'Separar corretamente os resíduos recicláveis.', 10, 3 FROM USUARIO WHERE EMAIL = 'inovatech@email.com';
INSERT INTO OBJETIVO (ID_USUARIO, TITULO, DESCRICAO, META, PROGRESSO) SELECT ID, 'Usar menos plástico', 'Evitar produtos descartáveis durante a semana.', 7, 4 FROM USUARIO WHERE EMAIL = 'inovatech@email.com';
INSERT INTO OBJETIVO (ID_USUARIO, TITULO, DESCRICAO, META, PROGRESSO) SELECT ID, 'Economizar água', 'Adotar hábitos para reduzir o consumo de água.', 10, 6 FROM USUARIO WHERE EMAIL = 'anaclara@email.com';
INSERT INTO OBJETIVO (ID_USUARIO, TITULO, DESCRICAO, META, PROGRESSO) SELECT ID, 'Mobilidade sustentável', 'Escolher meios de transporte menos poluentes.', 10, 5 FROM USUARIO WHERE EMAIL = 'anaclara@email.com';
INSERT INTO OBJETIVO (ID_USUARIO, TITULO, DESCRICAO, META, PROGRESSO) SELECT ID, 'Reduzir desperdícios', 'Evitar desperdícios de materiais no dia a dia.', 10, 7 FROM USUARIO WHERE EMAIL = 'pedrohenrique@email.com';
INSERT INTO OBJETIVO (ID_USUARIO, TITULO, DESCRICAO, META, PROGRESSO) SELECT ID, 'Usar transporte sustentável', 'Priorizar bicicleta, caminhada ou transporte público.', 10, 6 FROM USUARIO WHERE EMAIL = 'pedrohenrique@email.com';
INSERT INTO OBJETIVO (ID_USUARIO, TITULO, DESCRICAO, META, PROGRESSO) SELECT ID, 'Economizar energia', 'Reduzir o consumo de energia elétrica.', 10, 5 FROM USUARIO WHERE EMAIL = 'mariana@email.com';
INSERT INTO OBJETIVO (ID_USUARIO, TITULO, DESCRICAO, META, PROGRESSO) SELECT ID, 'Reduzir o plástico', 'Diminuir o uso de produtos descartáveis.', 10, 4 FROM USUARIO WHERE EMAIL = 'mariana@email.com';
INSERT INTO OBJETIVO (ID_USUARIO, TITULO, DESCRICAO, META, PROGRESSO) SELECT ID, 'Caminhar mais', 'Substituir pequenos deslocamentos de carro por caminhada.', 10, 4 FROM USUARIO WHERE EMAIL = 'joao@email.com';
INSERT INTO OBJETIVO (ID_USUARIO, TITULO, DESCRICAO, META, PROGRESSO) SELECT ID, 'Reciclar em casa', 'Separar corretamente materiais recicláveis.', 10, 5 FROM USUARIO WHERE EMAIL = 'joao@email.com';

-- Conteudos / noticias ambientais (antes em mock.ts)
INSERT INTO CONTEUDO (CATEGORIA, TITULO, RESUMO, FONTE, DATA_PUBLICACAO, URL) VALUES ('Mudanças climáticas', 'Aquecimento além de 1,5°C ameaça saúde, agricultura e economia', 'Especialistas alertam para os impactos de um planeta mais quente e defendem medidas urgentes para reduzir as emissões de gases.', 'Agência Brasil', DATE '2026-09-02', 'https://agenciabrasil.ebc.com.br/meio-ambiente/noticia/2026-09/aquecimento-alem-de-15degc-ameaca-saude-agricultura-e-economia');
INSERT INTO CONTEUDO (CATEGORIA, TITULO, RESUMO, FONTE, DATA_PUBLICACAO, URL) VALUES ('Mudanças climáticas', 'Relatório traz soluções para reduzir emissões de metano no país', 'Relatório reúne propostas para reduzir as emissões de metano e aponta medidas que podem ser adotadas por produtores, municípios e regiões.', 'Agência Brasil', DATE '2026-08-18', 'https://agenciabrasil.ebc.com.br/meio-ambiente/noticia/2026-08/relatorio-traz-solucoes-para-reduzir-emissoes-de-metano-no-pais');
INSERT INTO CONTEUDO (CATEGORIA, TITULO, RESUMO, FONTE, DATA_PUBLICACAO, URL) VALUES ('Sustentabilidade', 'Brasil promete restaurar 163 mil km² de terras degradadas até 2030', 'O país apresentou metas de recuperação de terras degradadas com ações voltadas à vegetação nativa, sistemas agroflorestais e áreas de conservação.', 'Agência Brasil', DATE '2026-08-25', 'https://agenciabrasil.ebc.com.br/meio-ambiente/noticia/2026-08/brasil-promete-restaurar-163-mil-km2-de-terras-degradadas-ate-2030');
INSERT INTO CONTEUDO (CATEGORIA, TITULO, RESUMO, FONTE, DATA_PUBLICACAO, URL) VALUES ('Sustentabilidade', 'Quatro em cada 10 brasileiros nunca ouviram falar em economia circular', 'A economia circular busca reutilizar, recuperar e reinserir produtos e recursos no ciclo produtivo, reduzindo o descarte.', 'Agência Brasil', DATE '2026-07-05', 'https://agenciabrasil.ebc.com.br/meio-ambiente/noticia/2026-07/quatro-em-cada-dez-brasileiros-nunca-ouviram-falar-em-economia-circular');
INSERT INTO CONTEUDO (CATEGORIA, TITULO, RESUMO, FONTE, DATA_PUBLICACAO, URL) VALUES ('Preservação', 'Senado aprova direito prioritário da criança a meio ambiente saudável', 'Projeto aprovado pelo Senado estabelece prioridade para o direito de crianças e adolescentes a um meio ambiente saudável.', 'Agência Brasil', DATE '2026-09-04', 'https://agenciabrasil.ebc.com.br/meio-ambiente/noticia/2026-09/senado-aprova-direito-prioritario-da-crianca-meio-ambiente-saudavel');
INSERT INTO CONTEUDO (CATEGORIA, TITULO, RESUMO, FONTE, DATA_PUBLICACAO, URL) VALUES ('Preservação', 'Imazon aponta desmatamento zero em 60% de terras indígenas na Amazônia', 'Levantamento aponta que 60% das terras indígenas analisadas registraram desmatamento zero no período avaliado.', 'Agência Brasil', DATE '2026-08-27', 'https://agenciabrasil.ebc.com.br/meio-ambiente/noticia/2026-08/imazon-aponta-desmatamento-zero-em-60-de-terras-indigenas-na-amazonia');
INSERT INTO CONTEUDO (CATEGORIA, TITULO, RESUMO, FONTE, DATA_PUBLICACAO, URL) VALUES ('Reciclagem', 'Tecnologia favorece descarte adequado de grandes eletrodomésticos', 'Novas soluções ajudam consumidores a encontrar formas adequadas de descarte para equipamentos que não podem ser simplesmente colocados no lixo comum.', 'Agência Brasil', DATE '2026-06-25', 'https://agenciabrasil.ebc.com.br/meio-ambiente/noticia/2026-06/tecnologia-favorece-descarte-adequado-de-grandes-eletrodomesticos');
INSERT INTO CONTEUDO (CATEGORIA, TITULO, RESUMO, FONTE, DATA_PUBLICACAO, URL) VALUES ('Reciclagem', 'Medidas podem ampliar a coleta seletiva e reduzir o desperdício', 'A ampliação da coleta seletiva e o melhor aproveitamento de materiais podem contribuir para reduzir a quantidade de resíduos descartados.', 'Agência Brasil', DATE '2026-08-18', 'https://agenciabrasil.ebc.com.br/meio-ambiente/noticia/2026-08/relatorio-traz-solucoes-para-reduzir-emissoes-de-metano-no-pais');
INSERT INTO CONTEUDO (CATEGORIA, TITULO, RESUMO, FONTE, DATA_PUBLICACAO, URL) VALUES ('Energia limpa', 'TV Brasil apresenta alternativas para os combustíveis do futuro', 'Biometano, e-metanol e hidrogênio verde são apresentados como alternativas produzidas a partir de fontes renováveis.', 'Agência Brasil', DATE '2026-05-25', 'https://agenciabrasil.ebc.com.br/meio-ambiente/noticia/2026-05/tv-brasil-caminhos-da-reportagem-apresenta-os-combustiveis-do-futuro');
INSERT INTO CONTEUDO (CATEGORIA, TITULO, RESUMO, FONTE, DATA_PUBLICACAO, URL) VALUES ('Energia limpa', 'Grande Muralha Verde avança em restauração de áreas na África', 'Iniciativa internacional combina recuperação de terras, segurança alimentar, acesso à energia limpa e maior resiliência diante das mudanças climáticas.', 'Agência Brasil', DATE '2026-08-08', 'https://agenciabrasil.ebc.com.br/meio-ambiente/noticia/2026-08/grande-muralha-verde-restaura-166-milhao-de-hectares');
INSERT INTO CONTEUDO (CATEGORIA, TITULO, RESUMO, FONTE, DATA_PUBLICACAO, URL) VALUES ('Meio ambiente', 'Iniciativas ambientais mostram como ações locais podem gerar impacto positivo', 'Projetos desenvolvidos por comunidades e organizações ajudam a fortalecer a conscientização e a preservação ambiental.', 'Agência Brasil', DATE '2026-08-30', 'https://agenciabrasil.ebc.com.br/meio-ambiente');

-- ------------------------------------------------------------------
-- Como criar um ADMINISTRADOR (nao ha admin com senha padrao no script):
-- 1) cadastre a conta normalmente pelo site;  2) execute, com o seu e-mail:
--    UPDATE USUARIO SET PERFIL = 'ADMIN' WHERE EMAIL = 'seu@email.com';
--    COMMIT;
-- ------------------------------------------------------------------

COMMIT;
