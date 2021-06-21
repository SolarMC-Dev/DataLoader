
CREATE TABLE user_ids (
  id INT AUTO_INCREMENT PRIMARY KEY,
  uuid BINARY(16) NOT NULL,
  UNIQUE INDEX uuid_uniqueness (uuid)
);

CREATE FUNCTION insert_or_get_user_id
  (mc_uuid BINARY(16))
  RETURNS INT
  MODIFIES SQL DATA
  BEGIN
    DECLARE found_id INT;
    SELECT id INTO found_id FROM user_ids WHERE uuid = mc_uuid;
    IF found_id IS NOT NULL THEN
      RETURN found_id;
    ELSE
      INSERT INTO user_ids (uuid) VALUES (mc_uuid);
      RETURN LAST_INSERT_ID();
    END IF;
  END;

-- A password row can be in 1 of 4 states:
-- 1. Not created, in which case the row does not exist
-- 2. Manually created, in which case the row exists and all fields are filled
-- 3. Automatically created, in which case the fields are 0 or null
-- #2 is typical for cracked users, #3 for premium users

CREATE TABLE auth_passwords (
  username VARCHAR(16) PRIMARY KEY,
  iterations TINYINT NOT NULL,
  memory INT NOT NULL,
  password_hash BINARY(64) NULL,
  password_salt BINARY(32) NULL,
  wants_migration BIT(1) NOT NULL
);

CREATE FUNCTION insert_automatic_account_and_get_user_id
  (mc_uuid BINARY(16),
  mc_username VARCHAR(16))
  RETURNS INT
  MODIFIES SQL DATA
  BEGIN
    INSERT INTO auth_passwords (username, iterations, memory, wants_migration) VALUES (mc_username, 0, 0, 0);
    RETURN insert_or_get_user_id(mc_uuid);
  END;

CREATE FUNCTION migrate_to_premium_and_get_user_id
  (offline_uuid BINARY(16),
  online_uuid BINARY(16),
  mc_username VARCHAR(16))
  RETURNS INT
  MODIFIES SQL DATA
  BEGIN
    UPDATE user_ids SET uuid = online_uuid WHERE uuid = offline_uuid;
    UPDATE auth_passwords
      SET iterations = 0, memory = 0, password_hash = NULL, password_salt = NULL
      WHERE username = mc_username;
    RETURN (SELECT id FROM user_ids WHERE uuid = online_uuid);
  END;

CREATE FUNCTION insert_cracked_account_and_get_user_id
  (username_arg VARCHAR(16),
  uuid_arg BINARY(16),
  iterations_arg TINYINT,
  memory_arg INT,
  password_hash_arg BINARY(64),
  password_salt_arg BINARY(32))
  RETURNS INT
  MODIFIES SQL DATA
  BEGIN
    -- Catch error code 1062 / ER_DUP_ENTRY
    DECLARE EXIT HANDLER FOR 1062
    BEGIN
      RETURN -1;
    END;
    INSERT INTO auth_passwords (username, iterations, memory, password_hash, password_salt, wants_migration)
      VALUES (username_arg, iterations_arg, memory_arg, password_hash_arg, password_salt_arg, 0);
    INSERT INTO user_ids (uuid) VALUES (uuid_arg);
    RETURN LAST_INSERT_ID();
  END;
