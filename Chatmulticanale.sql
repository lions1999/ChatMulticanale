
-- RECREATE USERS ---------------------------------------------------------------------------------------
DROP USER IF EXISTS 'login';
CREATE USER 'login'@'%' IDENTIFIED WITH mysql_native_password BY 'login';

DROP USER IF EXISTS 'dipendente';
CREATE USER 'dipendente'@'%' IDENTIFIED WITH mysql_native_password BY 'dipendente';

DROP USER IF EXISTS 'capoProgetto';
CREATE USER 'capoProgetto'@'%' IDENTIFIED WITH mysql_native_password BY 'capoProgetto';

DROP USER IF EXISTS 'amministratore';
CREATE USER 'amministratore'@'%' IDENTIFIED WITH mysql_native_password BY 'amministratore';

-- RECREATE SCHEMA --------------------------------------------------------------------------------------
DROP SCHEMA IF EXISTS chatmulticanale;
CREATE SCHEMA chatmulticanale;
USE chatmulticanale;

-- RECREATE TABLE ---------------------------------------------------------------------------------------

DROP TABLE IF EXISTS dipendente;
CREATE TABLE dipendente (
    username VARCHAR(45) PRIMARY KEY NOT NULL,
    isAdmin BOOLEAN DEFAULT FALSE NOT NULL
) ENGINE = InnoDB;


DROP TABLE IF EXISTS progetto;
CREATE TABLE progetto (
    nomeProgetto VARCHAR(45) PRIMARY KEY NOT NULL
) ENGINE = InnoDB;


DROP TABLE IF EXISTS canale;
CREATE TABLE canale (
    privato BOOLEAN NOT NULL,
    nomeCanale VARCHAR(45) NOT NULL,
    nomeProgetto VARCHAR(45) NOT NULL,
    PRIMARY KEY (nomeCanale, nomeProgetto),
    FOREIGN KEY (nomeProgetto) REFERENCES progetto(nomeProgetto)
) ENGINE = InnoDB;


DROP TABLE IF EXISTS credenziali;
CREATE TABLE credenziali (
    username VARCHAR(45) NOT NULL,
    password VARCHAR(45) NOT NULL,
    PRIMARY KEY (username,password),
    FOREIGN KEY (username) REFERENCES dipendente(username) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB;

DROP TABLE IF EXISTS messaggio;
CREATE TABLE messaggio (
    id INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    privato BOOLEAN NOT NULL,  -- 1 = privato, 0 = pubblico
    risposta INT,
    canale VARCHAR(45) NOT NULL,
    nomeProgetto VARCHAR(45) NOT NULL,
    contenuto TINYTEXT NOT NULL,
    mittente VARCHAR(45) NOT NULL,
    data DATETIME DEFAULT CURRENT_TIMESTAMP, -- colonna per la data e l'ora corrente
    FOREIGN KEY (risposta) REFERENCES messaggio(id),
    FOREIGN KEY (canale,nomeProgetto) REFERENCES canale(nomeCanale,nomeProgetto),
    FOREIGN KEY (mittente) REFERENCES dipendente(username),
    INDEX (canale,nomeProgetto)
) ENGINE = InnoDB;

CREATE TABLE appartiene_utenti_canali (
    username VARCHAR(45) NOT NULL,
    nomeCanale VARCHAR(45) NOT NULL,
    nomeProgetto VARCHAR(45) NOT NULL,
    PRIMARY KEY (username, nomeCanale, nomeProgetto),
    FOREIGN KEY (username) REFERENCES dipendente(username),
    FOREIGN KEY (nomeCanale, nomeProgetto) REFERENCES canale(nomeCanale, nomeProgetto)
) ENGINE = InnoDB;


DROP TABLE IF EXISTS RuoloInProgetto;
CREATE TABLE RuoloInProgetto (
    username VARCHAR(45) NOT NULL,
    nomeProgetto VARCHAR(45) NOT NULL,
    ruolo ENUM('DIPENDENTE', 'CAPO_PROGETTO','AMMINISTRATORE') NOT NULL,
    PRIMARY KEY (username, nomeProgetto),
    FOREIGN KEY (username) REFERENCES dipendente(username),
    FOREIGN KEY (nomeProgetto) REFERENCES progetto(nomeProgetto)
) ENGINE = InnoDB;

-- GRANT PRIVILEGES -------------------------------------------------------------------------------------

--          GRANT TO LOGIN
GRANT SELECT ON chatmulticanale.credenziali TO 'login'@'%'; 
GRANT SELECT ON chatmulticanale.progetto TO 'dipendente'@'%';
GRANT SELECT ON chatmulticanale.progetto TO 'capoProgetto'@'%';
GRANT SELECT ON chatmulticanale.ruoloinprogetto TO 'capoProgetto'@'%';
GRANT SELECT ON chatmulticanale.progetto TO 'amministratore'@'%';
GRANT SELECT ON chatmulticanale.messaggio TO 'dipendente'@'%';
GRANT SELECT ON chatmulticanale.messaggio TO 'capoProgetto'@'%';
GRANT SELECT ON chatmulticanale.dipendente TO 'amministratore'@'%';
-- GRANT ALL PRIVILEGES ON chatmulticanale.* TO 'login'@'localhost';

-- (RE)CREATE TRIGGERS -----------------------------------------------------------------------------------
DELIMITER !

DROP TRIGGER IF EXISTS before_insert_messaggio!
CREATE TRIGGER before_insert_messaggio 
BEFORE INSERT ON messaggio
FOR EACH ROW 
BEGIN
    DECLARE total_participants INT;
    DECLARE is_channel_private BOOLEAN;

    -- Controlla se il canale è privato
    SELECT privato INTO is_channel_private
    FROM canale
    WHERE nomeCanale = NEW.canale AND nomeProgetto = NEW.nomeProgetto;

    -- Se il messaggio è marcato come privato e anche il canale lo è
    IF NEW.privato = 1 AND is_channel_private = 1 THEN

        -- Controlla il numero di partecipanti nel canale
        SELECT COUNT(*) INTO total_participants
        FROM appartiene_utenti_canali
        WHERE nomeCanale = NEW.canale AND nomeProgetto = NEW.nomeProgetto;

        -- Se il canale non ha esattamente due partecipanti
        IF total_participants <> 2 THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Il canale non ha esattamente due partecipanti';
        END IF;

        -- Assicurati che il mittente appartenga al canale
        IF NOT EXISTS (SELECT 1 FROM appartiene_utenti_canali WHERE nomeCanale = NEW.canale AND username = NEW.mittente) THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Il mittente non appartiene al canale privato';
        END IF;
    ELSEIF NEW.privato = 1 AND is_channel_private = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Il messaggio è marcato come privato, ma il canale non lo è';
    ELSEIF NEW.privato = 0 AND is_channel_private = 1 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Il canale è marcato come privato, ma il messaggio non lo è';
    END IF;

END ! 

DROP TRIGGER IF EXISTS verifica_esistenza_messaggio_risposta_before_insert!
CREATE TRIGGER verifica_esistenza_messaggio_risposta_before_insert
BEFORE INSERT ON messaggio
FOR EACH ROW 
BEGIN
    DECLARE messaggio_esistente INT;

    -- Solo se 'risposta' è impostato, altrimenti non è una risposta
    IF NEW.risposta IS NOT NULL THEN
        SELECT COUNT(*)
        INTO messaggio_esistente
        FROM messaggio
        WHERE id = NEW.risposta;

        IF messaggio_esistente = 0 THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Il messaggio a cui si sta cercando di rispondere non esiste!';
        END IF;
    END IF;
END !

-- Trigger per verificare l'esistenza del dipendente
DROP TRIGGER IF EXISTS verifica_esistenza_dipendente_before_insert!
CREATE TRIGGER verifica_esistenza_dipendente_before_insert 
BEFORE INSERT ON RuoloInProgetto
FOR EACH ROW
BEGIN
    IF NOT EXISTS (SELECT 1 FROM dipendente WHERE username = NEW.username) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Il dipendente specificato non esiste.';
    END IF;
END !

-- Trigger per verificare l'esistenza del progetto
DROP TRIGGER IF EXISTS verifica_esistenza_progetto_before_insert!
CREATE TRIGGER verifica_esistenza_progetto_before_insert 
BEFORE INSERT ON RuoloInProgetto
FOR EACH ROW
BEGIN
    IF NOT EXISTS (SELECT 1 FROM progetto WHERE nomeProgetto = NEW.nomeProgetto) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Il progetto specificato non esiste.';
    END IF;
END !

-- Trigger per verificare l'esistenza dell'utente
DROP TRIGGER IF EXISTS verifica_esistenza_utente_before_insert!
CREATE TRIGGER verifica_esistenza_utente_before_insert
BEFORE INSERT ON appartiene_utenti_canali
FOR EACH ROW 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM dipendente WHERE username = NEW.username) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Il dipendente specificato non esiste.';
    END IF;
END !

-- Trigger per verificare l'esistenza del canale
DROP TRIGGER IF EXISTS verifica_esistenza_canale_before_insert!
CREATE TRIGGER verifica_esistenza_canale_before_insert
BEFORE INSERT ON appartiene_utenti_canali
FOR EACH ROW 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM canale WHERE nomeCanale = NEW.nomeCanale AND nomeProgetto = NEW.nomeProgetto) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Il canale specificato non esiste per il progetto dato.';
    END IF;
END !

-- Trigger per verificare esistenza di un progetto
DROP TRIGGER IF EXISTS verifica_esistenza_progetto_before_insert_canale!
CREATE TRIGGER verifica_esistenza_progetto_before_insert_canale
BEFORE INSERT ON canale
FOR EACH ROW 
BEGIN
    -- Verifica se il progetto associato al canale esiste
    IF NOT EXISTS (SELECT 1 FROM progetto WHERE nomeProgetto = NEW.nomeProgetto) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Il progetto specificato non esiste.';
    END IF;
END !


-- Trigger per verificare che quando inserisco un utente in un proggetto l'utente non sia già all'interno del progetto
DROP TRIGGER IF EXISTS before_insert_ruoloinprogetto!
CREATE TRIGGER before_insert_ruoloinprogetto
BEFORE INSERT ON RuoloInProgetto
FOR EACH ROW 
BEGIN
    DECLARE utente_esistente INT;

    -- Controlla se l'utente è già associato al progetto
    SELECT COUNT(*) INTO utente_esistente
    FROM RuoloInProgetto 
    WHERE username = NEW.username AND nomeProgetto = NEW.nomeProgetto;

    IF utente_esistente > 0 THEN
        -- Se l'utente è già associato al progetto, genera un errore
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Utente già all\'interno di questo progetto.';
    END IF;
END !



-- (RE)CREATE PROCEDURES ---------------------------------------------------------------------------------
-- SOTTO OGNI PROCEDURA TROVIAMO I RISPETTIVI GRANT PER OGNI UTENTE


-- LD
DROP PROCEDURE IF EXISTS ListaDipendentiInCanale!
CREATE PROCEDURE ListaDipendentiInCanale(IN nome_del_canale VARCHAR(45), IN nome_del_progetto VARCHAR(45))
BEGIN
	SET TRANSACTION ISOLATION LEVEL READ COMMITTED;

	SELECT a.username, r.ruolo
	FROM appartiene_utenti_canali a
	JOIN RuoloInProgetto r ON a.username = r.username AND a.nomeProgetto = r.nomeProgetto
	WHERE a.nomeCanale = nome_del_canale AND a.nomeProgetto = nome_del_progetto;
END!
GRANT EXECUTE ON PROCEDURE chatmulticanale.ListaDipendentiInCanale TO 'dipendente'@'%'!
GRANT EXECUTE ON PROCEDURE chatmulticanale.ListaDipendentiInCanale TO 'capoProgetto'@'%'!



-- LM
DROP PROCEDURE IF EXISTS ListaMessaggiInCanale!
CREATE PROCEDURE ListaMessaggiInCanale(IN nome_del_canale VARCHAR(45), IN nome_del_progetto VARCHAR(45))
BEGIN
	SET TRANSACTION ISOLATION LEVEL READ COMMITTED;

	SELECT m.*
	FROM messaggio m
	JOIN canale c ON m.canale = c.nomeCanale
	WHERE m.canale = nome_del_canale AND c.nomeProgetto = nome_del_progetto;
END !
GRANT EXECUTE ON PROCEDURE chatmulticanale.ListaMessaggiInCanale TO 'dipendente'@'%'!
GRANT EXECUTE ON PROCEDURE chatmulticanale.ListaMessaggiInCanale TO 'capoProgetto'@'%'!

-- IM / RM1 sono utilizzate attraveso questa procedura ma vengono disambiguate dai trigger e dagli attributi all'interno
DROP PROCEDURE IF EXISTS InviaMessaggio!
CREATE PROCEDURE InviaMessaggio(
    IN contenuto_messaggio TINYTEXT, 
    IN risposta INT,
    IN is_privato BOOLEAN, 
    IN nomeProgetto VARCHAR(45),
    IN mittente_username VARCHAR(45), 
    IN nome_del_canale VARCHAR(45)
)
BEGIN
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN 
		ROLLBACK;
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Errore nell''invio del messaggio.';
    END;
        
	SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
    START TRANSACTION;

    INSERT INTO messaggio(privato, canale, nomeProgetto, risposta, contenuto, mittente)
    VALUES(is_privato, nome_del_canale, nomeProgetto, risposta, contenuto_messaggio, mittente_username);
    
    COMMIT;
END !
GRANT EXECUTE ON PROCEDURE chatmulticanale.InviaMessaggio TO 'dipendente'@'%'!
GRANT EXECUTE ON PROCEDURE chatmulticanale.InviaMessaggio TO 'capoProgetto'@'%'!


-- RM2
DROP PROCEDURE IF EXISTS InviaRispostaPrivata!
CREATE PROCEDURE InviaRispostaPrivata(
    IN contenutoMessaggio TINYTEXT,
    IN usernameMittente VARCHAR(45),
    IN idRisposta INT,
    IN isPrivato BOOLEAN,
    IN InNomeProgetto VARCHAR(45)
)
BEGIN
    DECLARE canaleEsistente INT;
    DECLARE nomeCanaleEsistenze VARCHAR(45);
    DECLARE nomeDestinatario VARCHAR(45);
    
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Errore nell''invio della risposta privata.';
    END;

    SET TRANSACTION ISOLATION LEVEL REPEATABLE READ;
    START TRANSACTION;

    -- Trova il nome dell'utente a cui stai rispondendo usando l'ID del messaggio di risposta
    SELECT mittente INTO nomeDestinatario FROM messaggio WHERE id = idRisposta;

    -- Verifica se il canale esiste in uno dei due formati
    SELECT COUNT(*) INTO canaleEsistente
    FROM canale 
    WHERE (((nomeCanale = CONCAT(usernameMittente, '_', nomeDestinatario))
           OR (nomeCanale = CONCAT(nomeDestinatario, '_', usernameMittente)))
          AND nomeProgetto = InNomeProgetto);

    -- Se il canale non esiste, crealo
    IF canaleEsistente = 0 THEN
        INSERT INTO canale (nomeCanale, privato, nomeProgetto) 
        VALUES (CONCAT(usernameMittente, '_', nomeDestinatario), TRUE, InNomeProgetto);

        -- Aggiorna la tabella appartiene_utenti_canali
        INSERT INTO appartiene_utenti_canali(username, nomeCanale, nomeProgetto) 
        VALUES (usernameMittente, CONCAT(usernameMittente, '_', nomeDestinatario), InNomeProgetto);
        INSERT INTO appartiene_utenti_canali(username, nomeCanale, nomeProgetto) 
        VALUES (nomeDestinatario, CONCAT(usernameMittente, '_', nomeDestinatario), InNomeProgetto);
    END IF;
    
    SELECT nomeCanale INTO nomeCanaleEsistenze
    FROM canale
    WHERE (((nomeCanale = CONCAT(usernameMittente, '_', nomeDestinatario))
           OR (nomeCanale = CONCAT(nomeDestinatario, '_', usernameMittente)))
          AND nomeProgetto = InNomeProgetto);

    -- Ora puoi inserire il messaggio
    INSERT INTO messaggio(privato, canale, nomeProgetto, contenuto, mittente, risposta)
    VALUES(isPrivato,nomeCanaleEsistenze, InNomeProgetto, contenutoMessaggio, usernameMittente, idRisposta);
    SELECT LAST_INSERT_ID();
    
    COMMIT;
    
END !
GRANT EXECUTE ON PROCEDURE chatmulticanale.InviaRispostaPrivata TO 'dipendente'@'%'!
GRANT EXECUTE ON PROCEDURE chatmulticanale.InviaRispostaPrivata TO 'capoProgetto'@'%'!

-- LC1 / LC2 sono racchiuse dalla stessa poichè differenziate dal ruolo dell'utente che sta effettuando l'operazione, se CAPO vede tutti i canali di un progetto se DIPENDENTE solo quelli in cui appartiene
DROP PROCEDURE IF EXISTS ListaCanaliPerUtenteProgetto!
CREATE PROCEDURE ListaCanaliPerUtenteProgetto(
    IN input_nomeProgetto VARCHAR(45),
    IN input_username VARCHAR(45),
    IN input_ruolo ENUM('DIPENDENTE', 'CAPO_PROGETTO', 'AMMINISTRATORE')
)
BEGIN
	SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
    
	-- Se l'utente è un CAPO_PROGETTO
	IF input_ruolo = 'CAPO_PROGETTO' THEN
		SELECT 
			c.nomeCanale, 
			c.nomeProgetto, 
			c.Privato, 
			(auc.username IS NOT NULL) AS AppartieneUtente
		FROM canale c
		LEFT JOIN appartiene_utenti_canali auc ON c.nomeCanale = auc.nomeCanale AND c.nomeProgetto = auc.nomeProgetto AND auc.username = input_username
		WHERE c.nomeProgetto = input_nomeProgetto;

	-- Se l'utente è un DIPENDENTE
	ELSEIF input_ruolo = 'DIPENDENTE' THEN
		SELECT 
			c.nomeCanale, 
			c.nomeProgetto, 
			c.Privato
		FROM canale c
		JOIN appartiene_utenti_canali auc ON c.nomeCanale = auc.nomeCanale AND c.nomeProgetto = auc.nomeProgetto
		WHERE auc.username = input_username AND auc.nomeProgetto = input_nomeProgetto;

	-- Se l'utente è un AMMINISTRATORE (non restituisce canali in quanto esterno ai progetti)
	ELSEIF input_ruolo = 'AMMINISTRATORE' THEN
		SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'L\'amministratore non ha accesso ai canali interni ai progetti.';
	END IF;
END ! 
GRANT EXECUTE ON PROCEDURE chatmulticanale.ListaCanaliPerUtenteProgetto TO 'dipendente'@'%'!
GRANT EXECUTE ON PROCEDURE chatmulticanale.ListaCanaliPerUtenteProgetto TO 'capoProgetto'@'%'!



-- LP
DROP PROCEDURE IF EXISTS getProgettiUtente!
CREATE PROCEDURE getProgettiUtente(IN input_username VARCHAR(45))
BEGIN
	SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
    
	SELECT r.nomeProgetto, r.ruolo 
	FROM RuoloInProgetto r 
	WHERE r.username = input_username;
END !
GRANT EXECUTE ON PROCEDURE chatmulticanale.getProgettiUtente TO 'dipendente'@'%'!
GRANT EXECUTE ON PROCEDURE chatmulticanale.getProgettiUtente TO 'capoProgetto'@'%'!


-- IP
DROP PROCEDURE IF EXISTS inserisciNuovoProgetto!
CREATE PROCEDURE inserisciNuovoProgetto(
    IN nome_nuovo_progetto VARCHAR(45),
    IN capo_progetto_username VARCHAR(45)
)
BEGIN
    DECLARE dipendente_exists INT;
    
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    
    SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
    START TRANSACTION;

    -- Verifica se il capo progetto designato esiste
    SELECT COUNT(*) INTO dipendente_exists 
    FROM dipendente 
    WHERE username = capo_progetto_username;

    IF dipendente_exists = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Il dipendente specificato non esiste.';
    ELSE
        -- Creazione del nuovo progetto
        INSERT INTO progetto (nomeProgetto)
        VALUES (nome_nuovo_progetto);
        
        -- Assegna il ruolo di capo progetto all'utente designato per il progetto appena creato
        INSERT INTO RuoloInProgetto (username, nomeProgetto, ruolo)
        VALUES (capo_progetto_username, nome_nuovo_progetto, 'CAPO_PROGETTO');
        
        -- Creazione del canale pubblico associato al progetto appena creato
        INSERT INTO canale (privato, nomeCanale, nomeProgetto)
        VALUES (false, CONCAT(nome_nuovo_progetto, '_pubblico'), nome_nuovo_progetto);
        
        -- Inserisco il nuovo capo progetto nel canale appena creato
        INSERT INTO appartiene_utenti_canali (username, nomeCanale, nomeProgetto)
        VALUES (capo_progetto_username, CONCAT(nome_nuovo_progetto, '_pubblico'), nome_nuovo_progetto);
    END IF;
    
    COMMIT;
END !
GRANT EXECUTE ON PROCEDURE chatmulticanale.inserisciNuovoProgetto TO 'amministratore'@'%'!


-- ID1
DROP PROCEDURE IF EXISTS InserisciNuovoUtente!
CREATE PROCEDURE InserisciNuovoUtente(
    IN nuovoUsername VARCHAR(45), 
    IN nuovaPassword VARCHAR(45)
)
BEGIN

	DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    
    SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
    START TRANSACTION;
    
    -- Inserimento in dipendente
    INSERT INTO dipendente(username) VALUES (nuovoUsername);

    -- Inserimento in credenziali
    INSERT INTO credenziali(username, password) VALUES (nuovoUsername, nuovaPassword);
    
    COMMIT;
END !
GRANT EXECUTE ON PROCEDURE chatmulticanale.InserisciNuovoUtente TO 'amministratore'@'%'!


-- ID2 viene inserito come dipendente poichè il capo progetto è scelto alla creazione del progetto
DROP PROCEDURE IF EXISTS InserisciUtenteInProgetto!
CREATE PROCEDURE InserisciUtenteInProgetto(
    IN inputUsername VARCHAR(45),
    IN nomeProgetto VARCHAR(45)
)
BEGIN

	DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    
    SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
    START TRANSACTION;

    -- Inserisci l'utente nel progetto con il ruolo specificato
    INSERT INTO RuoloInProgetto(username, nomeProgetto, ruolo) 
    VALUES (inputUsername, nomeProgetto, 'DIPENDENTE');
    
    COMMIT;
END !
GRANT EXECUTE ON PROCEDURE chatmulticanale.InserisciUtenteInProgetto TO 'amministratore'@'%'!

-- ID3
DROP PROCEDURE IF EXISTS InserisciDipendenteInCanale!
CREATE PROCEDURE InserisciDipendenteInCanale(
    IN inputUsername VARCHAR(45),
    IN inputNomeCanale VARCHAR(45),
    IN inputNomeProgetto VARCHAR(45)
)
BEGIN

	DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    
    SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
    START TRANSACTION;

    INSERT INTO appartiene_utenti_canali(username, nomeCanale, nomeProgetto) 
    VALUES (inputUsername, inputNomeCanale, inputNomeProgetto);
    
    COMMIT;
END !

-- IC1
DROP PROCEDURE IF EXISTS inserisciCanalePubblico!
CREATE PROCEDURE inserisciCanalePubblico(
    IN nome_del_canale VARCHAR(45),
    IN nome_del_progetto VARCHAR(45),
    IN utenti_list VARCHAR(255)
)
BEGIN

	DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    
    SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;
    START TRANSACTION;

	INSERT INTO canale (nomeCanale, privato, nomeProgetto) 
	VALUES (nome_del_canale, FALSE, nome_del_progetto);
    
    SET @s = utenti_list;
    WHILE LOCATE(',', @s) > 0 DO
        INSERT INTO appartiene_utenti_canali(username, nomeCanale, nomeProgetto)
        VALUES (SUBSTRING(@s, 1, LOCATE(',', @s) - 1), nome_del_canale, nome_del_progetto);
        SET @s = SUBSTRING(@s, LOCATE(',', @s) + 1);
    END WHILE;

    INSERT INTO appartiene_utenti_canali(username, nomeCanale, nomeProgetto)
    VALUES (@s, nome_del_canale, nome_del_progetto);
    
    COMMIT;
END !
GRANT EXECUTE ON PROCEDURE chatmulticanale.inserisciCanalePubblico TO 'capoProgetto'@'%'!


-- IC2
DROP PROCEDURE IF EXISTS inserisciCanalePrivato!
CREATE PROCEDURE inserisciCanalePrivato(
    IN nome_del_canale VARCHAR(45),
    IN nome_del_progetto VARCHAR(45),
    IN utenti_list VARCHAR(255)
)
BEGIN

	DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    
    SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;
    START TRANSACTION;
    
	INSERT INTO canale (nomeCanale, privato, nomeProgetto) 
	VALUES (nome_del_canale, TRUE, nome_del_progetto);
    
    SET @s = utenti_list;
    WHILE LOCATE(',', @s) > 0 DO
        INSERT INTO appartiene_utenti_canali(username, nomeCanale, nomeProgetto)
        VALUES (SUBSTRING(@s, 1, LOCATE(',', @s) - 1), nome_del_canale, nome_del_progetto);
        SET @s = SUBSTRING(@s, LOCATE(',', @s) + 1);
    END WHILE;

    INSERT INTO appartiene_utenti_canali(username, nomeCanale, nomeProgetto)
    VALUES (@s, nome_del_canale, nome_del_progetto);
    
    COMMIT;
END !
GRANT EXECUTE ON PROCEDURE chatmulticanale.inserisciCanalePrivato TO 'capoProgetto'@'%'!



-- LG
DROP PROCEDURE IF EXISTS login!
CREATE PROCEDURE login (IN var_username VARCHAR(30), IN var_password VARCHAR(30))
BEGIN
	DECLARE var_role ENUM('AMMINISTRATORE', 'CAPO_PROGETTO', 'DIPENDENTE') DEFAULT 'DIPENDENTE';

	SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
    
	-- Verifica le credenziali
	IF EXISTS (SELECT 1 FROM credenziali WHERE username = var_username AND password = var_password) THEN
		-- Se l'utente è amministratore
		IF EXISTS (SELECT 1 FROM dipendente WHERE username = var_username AND isAdmin = TRUE) THEN
			SET var_role = 'AMMINISTRATORE';
			-- L'amministratore ha accesso a tutti i progetti: ritorna tutti i progetti
			SELECT 'AMMINISTRATORE' as Ruolo, nomeProgetto 
			FROM progetto;
		ELSE
			-- Chiamata alla procedura getProgettiUtente per ottenere i progetti e ruoli dell'utente
			CALL getProgettiUtente(var_username);
		END IF;
	ELSE
		SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Credenziali non valide.';
	END IF;
END !
GRANT EXECUTE ON PROCEDURE chatmulticanale.login TO 'login'@'%'!





DELIMITER ;
-- POPULATE --------------------------------------------------------------------------------------------
;




-- Inserisci credenziali

INSERT INTO chatmulticanale.dipendente VALUES ("admin",1);
INSERT INTO chatmulticanale.credenziali VALUES ("admin","admin");
CALL InserisciNuovoUtente('user1', '0');
CALL InserisciNuovoUtente('user2', '0');
CALL InserisciNuovoUtente('user3', '0');
CALL InserisciNuovoUtente('user4', '0');
CALL InserisciNuovoUtente('user5', '0');
CALL InserisciNuovoUtente('user6', '0');

-- Inserisci progetti con utente CAPO
CALL inserisciNuovoProgetto('ProgettoA','user2');
CALL inserisciNuovoProgetto('ProgettoB','user3');
CALL inserisciNuovoProgetto('ProgettoC','user4');
CALL inserisciNuovoProgetto('ProgettoD','user6');

-- Assegna utenti ai progetti con ruolo DIPENDENTE
CALL InserisciUtenteInProgetto('user1', 'ProgettoA');
CALL InserisciUtenteInProgetto('user2', 'ProgettoB');
CALL InserisciUtenteInProgetto('user3', 'ProgettoC');
CALL InserisciUtenteInProgetto('user3', 'ProgettoA');
CALL InserisciUtenteInProgetto('user4', 'ProgettoD');

-- Crea canali privati
CALL inserisciCanalePrivato('user1_user2', 'ProgettoA', 'user1,user2');
CALL inserisciCanalePrivato('user2_user3', 'ProgettoB', 'user2,user3');
CALL inserisciCanalePrivato('user3_user4', 'ProgettoD', 'user3,user4');




-- Aggiungi dipendenti ai canali
INSERT INTO appartiene_utenti_canali(username, nomeCanale, nomeProgetto) VALUES ('user1', 'ProgettoA_pubblico', 'ProgettoA');
INSERT INTO appartiene_utenti_canali(username, nomeCanale, nomeProgetto) VALUES ('user3', 'ProgettoA_pubblico', 'ProgettoA');
INSERT INTO appartiene_utenti_canali(username, nomeCanale, nomeProgetto) VALUES ('user2', 'ProgettoB_pubblico', 'ProgettoB');
INSERT INTO appartiene_utenti_canali(username, nomeCanale, nomeProgetto) VALUES ('user3', 'ProgettoC_pubblico', 'ProgettoC');
INSERT INTO appartiene_utenti_canali(username, nomeCanale, nomeProgetto) VALUES ('user4', 'ProgettoD_pubblico', 'ProgettoD');


-- Inserisci qualche messaggio
INSERT INTO messaggio(privato, canale, nomeProgetto, contenuto, mittente) VALUES (FALSE, 'ProgettoA_pubblico', 'ProgettoA', 'Ciao a tutti nel ProgettoA!', 'user1');
INSERT INTO messaggio(privato, canale, nomeProgetto, contenuto, mittente) VALUES (FALSE, 'ProgettoA_pubblico', 'ProgettoA', 'Benvenuti nel canale!', 'user2');
INSERT INTO messaggio(privato, canale, nomeProgetto, contenuto, mittente) VALUES (FALSE, 'ProgettoA_pubblico', 'ProgettoA', 'Ciao a tutti!', 'user3');
INSERT INTO messaggio(privato, canale, nomeProgetto, contenuto, mittente) VALUES (FALSE, 'ProgettoA_pubblico', 'ProgettoA', 'Di cosa ci occupiamo?', 'user1');
INSERT INTO messaggio(privato, canale, nomeProgetto, contenuto, mittente) VALUES (FALSE, 'ProgettoA_pubblico', 'ProgettoA', 'Ve lo spiegherò nei prossimi giorni', 'user2');
INSERT INTO messaggio(privato, canale, nomeProgetto, contenuto, mittente) VALUES (FALSE, 'ProgettoB_pubblico', 'ProgettoB', 'Iniziamo a lavorare sul ProgettoB', 'user3');
INSERT INTO messaggio(privato, canale, nomeProgetto, contenuto, mittente) VALUES (FALSE, 'ProgettoB_pubblico', 'ProgettoB', 'Non vedo l\'ora!', 'user2');
INSERT INTO messaggio(privato, canale, nomeProgetto, contenuto, mittente) VALUES (FALSE, 'ProgettoB_pubblico', 'ProgettoB', 'Spero faremo un buon lavoro', 'user3');
INSERT INTO messaggio(privato, canale, nomeProgetto, contenuto, mittente) VALUES (FALSE, 'ProgettoB_pubblico', 'ProgettoB', 'C\è un termine di consegna per questo progetto?', 'user2');
INSERT INTO messaggio(privato, canale, nomeProgetto, contenuto, mittente) VALUES (FALSE, 'ProgettoC_pubblico', 'ProgettoC', 'Abbiamo molto da fare nel ProgettoC', 'user3');
INSERT INTO messaggio(privato, canale, nomeProgetto, contenuto, mittente) VALUES (FALSE, 'ProgettoC_pubblico', 'ProgettoC', 'Ci sforzeremo il più possibile!', 'user4');
INSERT INTO messaggio(privato, canale, nomeProgetto, contenuto, mittente) VALUES (FALSE, 'ProgettoC_pubblico', 'ProgettoC', 'Rendiamo orgogliosi i nostri superiori!', 'user3');
INSERT INTO messaggio(privato, canale, nomeProgetto, contenuto, mittente) VALUES (FALSE, 'ProgettoC_pubblico', 'ProgettoC', 'Certo non possiamo fallire!', 'user4');
INSERT INTO messaggio(privato, canale, nomeProgetto, contenuto, mittente) VALUES (FALSE, 'ProgettoD_pubblico', 'ProgettoD', 'Facciamo un meeting per il ProgettoD', 'user4');
INSERT INTO messaggio(privato, canale, nomeProgetto, contenuto, mittente) VALUES (TRUE, 'user1_user2', 'ProgettoA', 'Ehi, possiamo discutere in privato?', 'user1');
INSERT INTO messaggio(privato, canale, nomeProgetto, contenuto, mittente) VALUES (TRUE, 'user2_user3', 'ProgettoB', 'Hai tempo per una chiamata dopo?', 'user2');


INSERT INTO messaggio(privato, canale,  nomeProgetto, contenuto, mittente) VALUES (FALSE, 'ProgettoC_pubblico', 'ProgettoC', 'Hai tempo per una chiamata dopo?', 'user4');

-- Ecco alcune risposte a messaggi precedenti
INSERT INTO messaggio(privato, canale, nomeProgetto, contenuto, mittente, risposta) VALUES (FALSE, 'ProgettoA_pubblico', 'ProgettoA','Sono pronto per iniziare!', 'user2', 1);
INSERT INTO messaggio(privato, canale, nomeProgetto, contenuto, mittente, risposta) VALUES (TRUE, 'user1_user2', 'ProgettoA','Certo, dimmi!', 'user2', 6);





FLUSH PRIVILEGES;