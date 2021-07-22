CREATE TABLE clans_clan_info (
  clan_id INT AUTO_INCREMENT PRIMARY KEY,
  clan_name VARCHAR(32) UNIQUE NOT NULL,
  clan_leader INT NOT NULL,
  clan_kills INT NOT NULL,
  clan_deaths INT NOT NULL,
  clan_assists INT NOT NULL,
  INDEX clan_name_index (clan_name)
);

CREATE TABLE clans_clan_membership (
  user_id INT NOT NULL,
  clan_id INT NOT NULL,
  FOREIGN KEY (clan_id) REFERENCES clans_clan_info (clan_id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES user_ids (id) ON DELETE CASCADE,
  PRIMARY KEY (user_id),
  INDEX clan_id_index (clan_id)
);

CREATE TABLE clans_clan_alliances (
  clan_id INT NOT NULL,
  ally_id INT NOT NULL,
  FOREIGN KEY (clan_id) REFERENCES clans_clan_info (clan_id) ON DELETE CASCADE,
  FOREIGN KEY (ally_id) REFERENCES clans_clan_info (clan_id) ON DELETE CASCADE,
  PRIMARY KEY (clan_id),
  UNIQUE ally_id_index (ally_id)
);

CREATE TABLE clans_clan_enemies (
  clan_id INT NOT NULL,
  enemy_id INT NOT NULL,
  FOREIGN KEY (clan_id) REFERENCES clans_clan_info (clan_id) ON DELETE CASCADE,
  FOREIGN KEY (enemy_id) REFERENCES clans_clan_info (clan_id) ON DELETE CASCADE,
  PRIMARY KEY (clan_id, enemy_id),
  INDEX enemy_id_index (enemy_id)
);

CREATE FUNCTION clans_add_member
  (clan_identifier INT,
  user_identifier INT)
  RETURNS BIT
  MODIFIES SQL DATA
  BEGIN
    -- Catch duplicate key errors
    DECLARE EXIT HANDLER FOR 1062
    BEGIN
      RETURN b'0';
    END;
    INSERT INTO clans_clan_membership (user_id, clan_id) VALUES (user_identifier, clan_identifier);
    RETURN b'1';
  END;

CREATE FUNCTION clans_add_ally
  (clan_identifier INT,
  ally_identifier INT)
  RETURNS TINYINT
  MODIFIES SQL DATA
  COMMENT 'Return value 0 indicates ally added, 1 already an ally, and 2 is an enemy'
  BEGIN
    -- Catch duplicate key errors from insert
    DECLARE EXIT HANDLER FOR 1062
    BEGIN
      RETURN 1;
    END;
    IF EXISTS (SELECT 1 FROM clans_clan_enemies WHERE clan_id = clan_identifier AND enemy_id = ally_identifier) THEN
      RETURN 2;
    END IF;
    INSERT INTO clans_clan_alliances (clan_id, ally_id) VALUES (clan_identifier, ally_identifier), (ally_identifier, clan_identifier);
    RETURN 0;
  END;

CREATE FUNCTION clans_add_enemy
  (clan_identifier INT,
  enemy_identifier INT)
  RETURNS TINYINT
  MODIFIES SQL DATA
  COMMENT 'Return value 0 indicates enemy added, 1 already an enemy, and 2 is an ally'
  BEGIN
    -- Catch duplicate key errors from insert
    DECLARE EXIT HANDLER FOR 1062
    BEGIN
      RETURN 1;
    END;
    IF EXISTS (SELECT 1 FROM clans_clan_alliances WHERE clan_id = clan_identifier AND ally_id = enemy_identifier) THEN
      RETURN 2;
    END IF;
    INSERT INTO clans_clan_enemies (clan_id, enemy_id) VALUES (clan_identifier, enemy_identifier);
    RETURN 0;
  END;

CREATE FUNCTION clans_create_new (name VARCHAR(32), leader_id INT) RETURNS TINYINT MODIFIES SQL DATA
  COMMENT 'Returns 0 if method call successful. Returns 1 if name is not unique.
  Returns 2 if user referenced by leader_id is already a clan member.'
  BEGIN
      -- Since they are all server-sided and therefore optimized by the engine AND there is no latency due to java-sql/io can i call
    -- select queries as much as i fucking please? /s (but is this okay?)
    IF EXISTS(SELECT 1 FROM clans_clan_info WHERE clan_name = name) THEN
      -- i'm programming sql functions like i program java - i don't like try/catching when possible, so
      -- why not just check explicitly for condition x/y - this one verifies explicitly uniqueness of clan name, so
      -- i can return whatever "error code" i want.
      RETURN 1;
    END IF;


    IF EXISTS(SELECT 1 FROM clans_clan_membership WHERE user_id = leader_id) THEN
      RETURN 2;
    END IF;


    INSERT INTO clans_clan_info (clan_name, clan_leader, clan_kills, clan_deaths, clan_assists) VALUES (name, leader_id, 0, 0, 0);
    INSERT INTO clans_clan_membership (user_id, clan_id) VALUES (leader_id, LAST_INSERT_ID());

    RETURN 0;
  END;

