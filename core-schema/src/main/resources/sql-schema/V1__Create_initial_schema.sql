CREATE TABLE user_ids (
  id INT AUTO_INCREMENT PRIMARY KEY,
  uuid BINARY(16) NOT NULL UNIQUE
);

CREATE TABLE credits (
  user_id INT PRIMARY KEY,
  balance NUMERIC(12, 3) NOT NULL,
  FOREIGN KEY (user_id) REFERENCES user_ids (id) ON DELETE CASCADE
);

CREATE TABLE kitpvp_statistics (
  user_id INT PRIMARY KEY,
  kills INT NOT NULL,
  deaths INT NOT NULL,
  assists INT NOT NULL,
  FOREIGN KEY (user_id) REFERENCES user_ids (id) ON DELETE CASCADE
);

CREATE TABLE kitpvp_kits_ids (
  kit_id INT AUTO_INCREMENT PRIMARY KEY,
  kit_name VARCHAR(32) NOT NULL UNIQUE
);

CREATE TABLE kitpvp_kits_ownership (
  user_id INT NOT NULL,
  kit_id INT NOT NULL,
  FOREIGN KEY (user_id) REFERENCES user_ids (id) ON DELETE CASCADE,
  FOREIGN KEY (kit_id) REFERENCES kitpvp_kits_ids (kit_id) ON DELETE CASCADE,
  PRIMARY KEY (user_id, kit_id)
);

CREATE TABLE kitpvp_kits_contents (
  kit_id INT NOT NULL,
  slot TINYINT NOT NULL,
  item BLOB NOT NULL,
  FOREIGN KEY (kit_id) REFERENCES kitpvp_kits_ids (kit_id) ON DELETE CASCADE,
  PRIMARY KEY (kit_id, slot)
);

CREATE TABLE prefs_flags (
  user_id INT PRIMARY KEY,
  enable_pms BOOLEAN NOT NULL,
  FOREIGN KEY (user_id) REFERENCES user_ids (id) ON DELETE CASCADE
);

CREATE TABLE friends_blocked_users (
  user_id INT NOT NULL,
  blocked_user_id INT NOT NULL,
  FOREIGN KEY (user_id) REFERENCES user_ids (id) ON DELETE CASCADE,
  FOREIGN KEY (blocked_user_id) REFERENCES user_ids (id) ON DELETE CASCADE,
  PRIMARY KEY (user_id, blocked_user_id)
);

CREATE TABLE friends_friended_users (
  user_id INT NOT NULL,
  friend_user_id INT NOT NULL,
  FOREIGN KEY (user_id) REFERENCES user_ids (id) ON DELETE CASCADE,
  FOREIGN KEY (friend_user_id) REFERENCES user_ids (id) ON DELETE CASCADE,
  PRIMARY KEY (user_id, friend_user_id)
);

-- A password row can be in 1 of 4 states:
-- 1. Not created, in which case the row does not exist
-- 2. Manually created, in which case the row exists and all fields are not null
-- 3. Automatically created, in which case iterations is 0 and other fields are empty
-- #2 is typical for cracked users, #3 for premium users

CREATE TABLE auth_passwords (
  username VARCHAR(16) PRIMARY KEY,
  iterations TINYINT NOT NULL,
  memory INT NOT NULL,
  password_hash BINARY(64) NOT NULL,
  password_salt BINARY(32) NOT NULL
)