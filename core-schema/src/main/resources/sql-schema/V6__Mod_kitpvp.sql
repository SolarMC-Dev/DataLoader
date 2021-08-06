
CREATE TABLE kitpvp_statistics (
  user_id INT PRIMARY KEY,
  kills INT NOT NULL DEFAULT 0,
  deaths INT NOT NULL DEFAULT 0,
  assists INT NOT NULL DEFAULT 0,
  current_killstreak INT NOT NULL DEFAULT 0,
  highest_killstreak INT NOT NULL DEFAULT 0,
  experience INT NOT NULL DEFAULT 0,
  bounty NUMERIC(15, 3) NOT NULL DEFAULT 0,

  FOREIGN KEY (user_id) REFERENCES user_ids (id) ON DELETE CASCADE,
  INDEX kills_index (kills),
  INDEX highest_killstreak_index (highest_killstreak),
  INDEX bounty_index (bounty),
  CONSTRAINT nonnegative_kills CHECK (kills >= 0),
  CONSTRAINT nonnegative_deaths CHECK (deaths >= 0),
  CONSTRAINT nonnegative_assists CHECK (assists >= 0),
  CONSTRAINT nonnegative_current_killstreak CHECK (current_killstreak >= 0),
  CONSTRAINT nonnegative_highest_killstreak CHECK (highest_killstreak >= 0),
  CONSTRAINT nonnegative_experience CHECK (experience >= 0),
  CONSTRAINT nonnegative_bounty CHECK (bounty >= 0)
);

CREATE TABLE kitpvp_kits_ids (
  kit_id INT AUTO_INCREMENT PRIMARY KEY,
  kit_name VARCHAR(32) NOT NULL,
  -- Seconds
  kit_cooldown INT NOT NULL,
  UNIQUE INDEX kit_name_uniqueness (kit_name)
);

CREATE TABLE kitpvp_kits_ownership (
  user_id INT NOT NULL,
  kit_id INT NOT NULL,
  FOREIGN KEY (user_id) REFERENCES user_ids (id) ON DELETE CASCADE,
  FOREIGN KEY (kit_id) REFERENCES kitpvp_kits_ids (kit_id) ON DELETE CASCADE,
  PRIMARY KEY (user_id, kit_id)
);

CREATE TABLE kitpvp_kits_cooldowns (
  user_id INT NOT NULL,
  kit_id INT NOT NULL,
  -- Unix seconds
  last_used BIGINT NOT NULL,
  FOREIGN KEY (user_id) REFERENCES user_ids (id) ON DELETE CASCADE,
  FOREIGN KEY (kit_id) REFERENCES kitpvp_kits_ids (kit_id) ON DELETE CASCADE,
  PRIMARY KEY (user_id, kit_id)
);

CREATE TABLE kitpvp_kits_contents (
  kit_id INT NOT NULL,
  slot TINYINT NOT NULL,
  item BLOB NOT NULL,
  FOREIGN KEY (kit_id) REFERENCES kitpvp_kits_ids (kit_id) ON DELETE CASCADE,
  PRIMARY KEY (kit_id, slot),
  CONSTRAINT nonnegative_slot CHECK (slot >= 0)
);

CREATE TABLE kitpvp_bounty_logs (
  bounty_claim INT AUTO_INCREMENT PRIMARY KEY,
  killer_id INT NOT NULL,
  victim_id INT NOT NULL,
  FOREIGN KEY (killer_id) REFERENCES user_ids (id) ON DELETE CASCADE,
  FOREIGN KEY (victim_id) REFERENCES user_ids (id) ON DELETE CASCADE
);

CREATE FUNCTION kitpvp_create_kit
  (kit_displayname VARCHAR(32),
  kit_cooldownseconds INT)
  RETURNS INT
  MODIFIES SQL DATA
  BEGIN
    -- Catch duplicate key errors
    DECLARE EXIT HANDLER FOR 1062
    BEGIN
      RETURN -1;
    END;
    INSERT INTO kitpvp_kits_ids (kit_name, kit_cooldown) VALUES (kit_displayname, kit_cooldownseconds);
    RETURN LAST_INSERT_ID();
  END;

CREATE FUNCTION kitpvp_add_kills
  (user_identifier INT,
  amount INT)
  RETURNS INT
  MODIFIES SQL DATA
  BEGIN
    UPDATE kitpvp_statistics SET kills = kills + amount WHERE user_id = user_identifier;
    RETURN (SELECT kills FROM kitpvp_statistics WHERE user_id = user_identifier);
  END;

CREATE FUNCTION kitpvp_add_deaths
  (user_identifier INT,
  amount INT)
  RETURNS INT
  MODIFIES SQL DATA
  BEGIN
    UPDATE kitpvp_statistics SET deaths = deaths + amount WHERE user_id = user_identifier;
    RETURN (SELECT deaths FROM kitpvp_statistics WHERE user_id = user_identifier);
  END;

CREATE FUNCTION kitpvp_add_assists
  (user_identifier INT,
  amount INT)
  RETURNS INT
  MODIFIES SQL DATA
  BEGIN
    UPDATE kitpvp_statistics SET assists = assists + amount WHERE user_id = user_identifier;
    RETURN (SELECT assists FROM kitpvp_statistics WHERE user_id = user_identifier);
  END;

CREATE FUNCTION kitpvp_add_experience
  (user_identifier INT,
  amount INT)
  RETURNS INT
  MODIFIES SQL DATA
  BEGIN
    UPDATE kitpvp_statistics SET experience = experience + amount WHERE user_id = user_identifier;
    RETURN (SELECT experience FROM kitpvp_statistics WHERE user_id = user_identifier);
  END;

CREATE PROCEDURE kitpvp_add_killstreak
  (user_identifier INT,
  amount INT,
  OUT new_current_killstreak INT,
  OUT new_highest_killstreak INT)
  MODIFIES SQL DATA
  BEGIN
    -- Requires simultaneous assignment, NOT default right-to-left behavior
    UPDATE kitpvp_statistics
      SET current_killstreak = current_killstreak + amount, highest_killstreak = GREATEST(highest_killstreak, current_killstreak + amount)
      WHERE user_id = user_identifier;
    SELECT current_killstreak, highest_killstreak INTO new_current_killstreak, new_highest_killstreak
      FROM kitpvp_statistics
      WHERE user_id = user_identifier;
  END;

CREATE FUNCTION kitpvp_reset_current_killstreak
  (user_identifier INT)
  RETURNS INT
  MODIFIES SQL DATA
  BEGIN
    DECLARE previous_current_killstreak INT;
    SELECT current_killstreak INTO previous_current_killstreak
      FROM kitpvp_statistics WHERE user_id = user_identifier;
    UPDATE kitpvp_statistics SET current_killstreak = 0 WHERE user_id = user_identifier;
    RETURN previous_current_killstreak;
  END;

CREATE FUNCTION kitpvp_add_bounty
  (user_identifier INT,
  amount NUMERIC(15, 3))
  RETURNS NUMERIC(15, 3)
  MODIFIES SQL DATA
  BEGIN
    UPDATE kitpvp_statistics SET bounty = bounty + amount WHERE user_id = user_identifier;
    RETURN (SELECT bounty FROM kitpvp_statistics WHERE user_id = user_identifier);
  END;

CREATE FUNCTION kitpvp_reset_bounty
  (user_identifier INT)
  RETURNS NUMERIC(15, 3)
  MODIFIES SQL DATA
  BEGIN
    DECLARE previous_bounty NUMERIC(15, 3);

    SELECT bounty INTO previous_bounty FROM kitpvp_statistics WHERE user_id = user_identifier;
    UPDATE kitpvp_statistics SET bounty = 0 WHERE user_id = user_identifier;
    RETURN previous_bounty;
  END;
