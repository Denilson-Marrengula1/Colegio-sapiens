CREATE TABLE turmas (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL,
    classe VARCHAR(80) NOT NULL,
    ano_letivo INTEGER NOT NULL,
    turno VARCHAR(30),
    CONSTRAINT uk_turmas_codigo_ano UNIQUE (codigo, ano_letivo)
);

CREATE TABLE encarregados (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    documento VARCHAR(40) NOT NULL UNIQUE,
    telefone VARCHAR(30) NOT NULL,
    email VARCHAR(160)
);

CREATE TABLE professores (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    numero_funcionario VARCHAR(30) NOT NULL UNIQUE,
    email VARCHAR(160),
    telefone VARCHAR(30)
);

CREATE TABLE alunos (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    numero_matricula VARCHAR(30) NOT NULL UNIQUE,
    data_nascimento DATE,
    turma_id BIGINT,
    CONSTRAINT fk_alunos_turma FOREIGN KEY (turma_id) REFERENCES turmas(id)
);

CREATE TABLE aluno_encarregado (
    aluno_id BIGINT NOT NULL,
    encarregado_id BIGINT NOT NULL,
    PRIMARY KEY (aluno_id, encarregado_id),
    CONSTRAINT fk_ae_aluno FOREIGN KEY (aluno_id) REFERENCES alunos(id),
    CONSTRAINT fk_ae_encarregado FOREIGN KEY (encarregado_id) REFERENCES encarregados(id)
);

CREATE TABLE professor_turma (
    professor_id BIGINT NOT NULL,
    turma_id BIGINT NOT NULL,
    PRIMARY KEY (professor_id, turma_id),
    CONSTRAINT fk_pt_professor FOREIGN KEY (professor_id) REFERENCES professores(id),
    CONSTRAINT fk_pt_turma FOREIGN KEY (turma_id) REFERENCES turmas(id)
);

CREATE TABLE utilizadores (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(60) NOT NULL UNIQUE,
    nome VARCHAR(120) NOT NULL,
    email VARCHAR(160),
    telefone VARCHAR(30),
    password_hash VARCHAR(100) NOT NULL,
    role ENUM('ADMIN', 'ALUNO', 'COORDENADOR', 'DIRECTOR', 'ENCARREGADO', 'PROFESSOR', 'SECRETARIA') NOT NULL,
    activo BOOLEAN NOT NULL,
    criado_em TIMESTAMP(6) NOT NULL,
    credenciais_definidas BOOLEAN NOT NULL,
    encarregado_id BIGINT UNIQUE,
    professor_id BIGINT UNIQUE,
    aluno_id BIGINT UNIQUE,
    CONSTRAINT fk_utilizadores_encarregado FOREIGN KEY (encarregado_id) REFERENCES encarregados(id),
    CONSTRAINT fk_utilizadores_professor FOREIGN KEY (professor_id) REFERENCES professores(id),
    CONSTRAINT fk_utilizadores_aluno FOREIGN KEY (aluno_id) REFERENCES alunos(id)
);
