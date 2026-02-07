USE ide_collaboratif;




CREATE TABLE if not exists utilisateurs
(
    id           INT PRIMARY KEY AUTO_INCREMENT,
    nom          VARCHAR(50)         NOT NULL,
    email        VARCHAR(100) UNIQUE NOT NULL,
    mot_de_passe VARCHAR(255)        NOT NULL
);

CREATE TABLE if not exists dossiers (
                                        id               INT AUTO_INCREMENT PRIMARY KEY,
                                        nom              VARCHAR(255)  NOT NULL,
                                        parent_id        INT           NULL,
                                        proprietaire_id  INT           NOT NULL,
                                        CONSTRAINT fk_dossiers_parent
                                            FOREIGN KEY (parent_id)
                                                REFERENCES dossiers(id)
                                                ON DELETE CASCADE,
                                        CONSTRAINT fk_dossiers_owner
                                            FOREIGN KEY (proprietaire_id)
                                                REFERENCES utilisateurs(id)
                                                ON DELETE CASCADE
);

CREATE TABLE if not exists fichiers
(
    id                INT PRIMARY KEY AUTO_INCREMENT,
    nom               VARCHAR(255) NOT NULL,
    dossier_id        INT NULL,
    contenu           TEXT,
    proprietaire_id   INT,
    date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (proprietaire_id) REFERENCES utilisateurs (id) ON DELETE CASCADE,
    FOREIGN KEY (dossier_id) REFERENCES dossiers (id) ON DELETE CASCADE
);

-- Nouvelle table pour gérer les droits d'accès
CREATE TABLE if not exists droits_acces (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    fichier_id  INT NOT NULL,
    utilisateur_id INT NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'editor',
    peut_modifier BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (fichier_id) REFERENCES fichiers(id) ON DELETE CASCADE,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    UNIQUE KEY unique_droit (fichier_id, utilisateur_id)
);


CREATE TABLE if not exists amis (
                      user_id_1   INT         NOT NULL,
                      user_id_2   INT         NOT NULL,
                      created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      PRIMARY KEY (user_id_1, user_id_2),
                      FOREIGN KEY (user_id_1)
                          REFERENCES utilisateurs(id)
                          ON DELETE CASCADE
                          ON UPDATE CASCADE,
                      FOREIGN KEY (user_id_2)
                          REFERENCES utilisateurs(id)
                          ON DELETE CASCADE
                          ON UPDATE CASCADE
);



CREATE TABLE if not exists favoris (
                         utilisateur_id  INT NOT NULL,
                         fichier_id      INT NOT NULL,
                         PRIMARY KEY (utilisateur_id, fichier_id),
                         CONSTRAINT fk_favoris_user
                             FOREIGN KEY (utilisateur_id)
                                 REFERENCES utilisateurs(id)
                                 ON DELETE CASCADE,
                         CONSTRAINT fk_favoris_file
                             FOREIGN KEY (fichier_id)
                                 REFERENCES fichiers(id)
                                 ON DELETE CASCADE
);


CREATE TABLE invitations (
                             id               INT AUTO_INCREMENT PRIMARY KEY,
                             fichier_id       INT NOT NULL,
                             inviter_id       INT NOT NULL,
                             destinataire_id       INT NOT NULL,
                             role VARCHAR(20) NOT NULL DEFAULT 'editor',
                             date_invite      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             CONSTRAINT fk_invite_file
                                 FOREIGN KEY (fichier_id) REFERENCES fichiers(id) ON DELETE CASCADE,
                             CONSTRAINT fk_invite_inviter
                                 FOREIGN KEY (inviter_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
                             CONSTRAINT fk_invite_invitee
                                 FOREIGN KEY (destinataire_id) REFERENCES utilisateurs(id) ON DELETE CASCADE
);

CREATE TABLE project_chat (
                              id INT AUTO_INCREMENT PRIMARY KEY,
                              project_id INT NOT NULL,
                              user_id    INT NOT NULL,
                              message    TEXT NOT NULL,
                              timestamp  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              FOREIGN KEY (project_id) REFERENCES dossiers(id)   ON DELETE CASCADE,
                              FOREIGN KEY (user_id)    REFERENCES utilisateurs(id) ON DELETE CASCADE
);


-- Table demande_ami
CREATE TABLE IF NOT EXISTS demande_ami (
                                           id INT AUTO_INCREMENT PRIMARY KEY,
                                           expediteur_id INT NOT NULL,
                                           destinataire_id INT NOT NULL,
                                           statut ENUM('EN_ATTENTE', 'ACCEPTEE', 'REFUSEE') NOT NULL DEFAULT 'EN_ATTENTE',
                                           date_demande TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                           date_reponse TIMESTAMP NULL,
                                           FOREIGN KEY (expediteur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
                                           FOREIGN KEY (destinataire_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
                                           UNIQUE KEY uq_demande (expediteur_id, destinataire_id),
                                           CHECK (expediteur_id != destinataire_id)
);

-- Table notifications
CREATE TABLE IF NOT EXISTS notifications (
                                             id INT AUTO_INCREMENT PRIMARY KEY,
                                             utilisateur_id INT NOT NULL,
                                             message TEXT NOT NULL,
                                             type ENUM('DEMANDE_AMI', 'DEMANDE_ACCEPTEE', 'DEMANDE_REFUSEE', 'SYSTEME', 'COLLABORATION') NOT NULL,
                                             lu BOOLEAN NOT NULL DEFAULT FALSE,
                                             date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                             FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS chat_messages (
                                             id INT AUTO_INCREMENT PRIMARY KEY,
                                             expediteur_id INT NOT NULL,
                                             destinataire_id INT NOT NULL,
                                             message TEXT NOT NULL,
                                             timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                             status ENUM('SENT', 'DELIVERED', 'READ') NOT NULL DEFAULT 'SENT',

                                             FOREIGN KEY (expediteur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
                                             FOREIGN KEY (destinataire_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,

                                             INDEX idx_expediteur (expediteur_id),
                                             INDEX idx_destinataire (destinataire_id),
                                             INDEX idx_conversation (expediteur_id, destinataire_id, timestamp)
);

-- Modifier la table amis pour ajouter la date de création de l'amitié
ALTER TABLE amis ADD COLUMN date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Index pour améliorer les performances des requêtes
CREATE INDEX idx_notifications_utilisateur ON notifications(utilisateur_id, lu);
CREATE INDEX idx_demande_ami_destinataire ON demande_ami(destinataire_id, statut);
CREATE INDEX idx_demande_ami_expediteur ON demande_ami(expediteur_id, statut);

DELIMITER //
CREATE TRIGGER if not exists amis_before_insert
    BEFORE INSERT ON amis
    FOR EACH ROW
BEGIN
    -- Empêche l'amitié avec soi-même
    IF NEW.user_id_1 = NEW.user_id_2 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Un utilisateur ne peut pas être ami avec lui-même';
    END IF;
    -- Échange si l'ordre est inversé
    IF NEW.user_id_1 > NEW.user_id_2 THEN
        SET @tmp = NEW.user_id_1;
        SET NEW.user_id_1 = NEW.user_id_2;
        SET NEW.user_id_2 = @tmp;
    END IF;
END;//
DELIMITER ;


