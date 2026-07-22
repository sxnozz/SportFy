-- Apaga o banco de dados antigo se ele existir, para começar do zero.
DROP DATABASE IF EXISTS Sportfy;

-- Cria o novo banco de dados.
CREATE DATABASE Sportfy;

-- Seleciona o banco de dados para usar.
USE Sportfy;

-- Tabela para armazenar os dados dos usuários.
CREATE TABLE Usuario (
    Id_usuario BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    Senha VARCHAR(255) NOT NULL, -- Aumentado para senhas criptografadas
    Email VARCHAR(157) NOT NULL UNIQUE, -- Email deve ser único
    Nome VARCHAR(157) NOT NULL,
    Foto_usuario BLOB NULL -- BLOB para a foto, pode ser nulo
);

-- Tabela para armazenar os dados dos eventos.
CREATE TABLE Evento (
    Id_evento BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    Id_usuario_criador BIGINT NOT NULL, -- Chave estrangeira para quem criou o evento
    Modalidade_evento VARCHAR(157) NOT NULL,
    Lugar VARCHAR(255) NOT NULL,
    Descricao VARCHAR(355),
    Data_hora_evento DATETIME NOT NULL,
    Horario_de_postagem DATETIME,
    FOREIGN KEY (Id_usuario_criador) REFERENCES Usuario(Id_usuario)
);

-- Tabela de ligação para registrar os participantes de um evento (Muitos-para-Muitos).
CREATE TABLE Evento_participante (
    Id_evento BIGINT NOT NULL,
    Id_usuario BIGINT NOT NULL,
    PRIMARY KEY (Id_evento, Id_usuario), -- A chave primária é a combinação dos dois IDs
    FOREIGN KEY (Id_evento) REFERENCES Evento(Id_evento),
    FOREIGN KEY (Id_usuario) REFERENCES Usuario(Id_usuario)
);

-- Tabela para armazenar os comentários de cada evento.
CREATE TABLE Comentario (
    Id_comentario BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    Id_evento BIGINT NOT NULL,
    Id_usuario BIGINT NOT NULL,
    Texto_comentario VARCHAR(300) NOT NULL,
    Horario_comentario DATETIME,
    FOREIGN KEY (Id_evento) REFERENCES Evento(Id_evento),
    FOREIGN KEY (Id_usuario) REFERENCES Usuario(Id_usuario)
);

-- Tabela unificada para armazenar todas as métricas de desempenho.
CREATE TABLE Metrica (
    Id_metrica BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    Id_usuario BIGINT NOT NULL,
    Dia_metrica DATE NOT NULL,
    Esporte VARCHAR(50) NOT NULL, -- 'Futebol', 'Basquete' ou 'Volei'
    
    -- Métricas de Futebol (NULL se não for futebol)
    Gols INT NULL,
    Assistencias_futebol INT NULL,
    Desarmes INT NULL,
    
    -- Métricas de Basquete (NULL se não for basquete)
    Pontos_basquete INT NULL,
    Assistencias_basquete INT NULL,
    Rebotes INT NULL,
    
    -- Métricas de Vôlei (NULL se não for vôlei)
    Pontos_volei INT NULL,
    Aces INT NULL,
    Bloqueios INT NULL,
    
    FOREIGN KEY (Id_usuario) REFERENCES Usuario(Id_usuario)
);