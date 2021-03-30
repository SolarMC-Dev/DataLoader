
CREATE TABLE user_ids (
  id INT AUTO_INCREMENT PRIMARY KEY,
  uuid BINARY(16) NOT NULL UNIQUE
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
    INSERT INTO auth_passwords (username) VALUES (mc_username);
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
