CREATE TABLE user_ids (
  id INT NOT NULL AUTO_INCREMENT UNIQUE,
  uuid BINARY(16) NOT NULL,
  PRIMARY KEY(uuid)
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

CREATE TABLE kitpvp_kits_names (
  kit_id INT AUTO_INCREMENT PRIMARY KEY,
  kit_name VARCHAR(32) NOT NULL UNIQUE
);

CREATE TABLE kitpvp_kits_ownership (
  user_id INT NOT NULL,
  kit_id INT NOT NULL,
  FOREIGN KEY (user_id) REFERENCES user_ids (id) ON DELETE CASCADE,
  FOREIGN KEY (kit_id) REFERENCES kitpvp_kits_names (kit_id) ON DELETE CASCADE,
  INDEX (user_id),
  PRIMARY KEY (user_id, kit_id)
);

CREATE TABLE kitpvp_kits_contents (
  kit_id INT NOT NULL,
  slot TINYINT NOT NULL,
  item BLOB NOT NULL,
  FOREIGN KEY (kit_id) REFERENCES kitpvp_kits_names (kit_id) ON DELETE CASCADE,
  INDEX (kit_id),
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
  INDEX (user_id),
  UNIQUE (user_id,blocked_user_id)
);

CREATE TABLE friends_friended_users (
  user_id INT NOT NULL,
  friend_user_id INT NOT NULL,
  FOREIGN KEY (user_id) REFERENCES user_ids (id) ON DELETE CASCADE,
  FOREIGN KEY (friend_user_id) REFERENCES user_ids (id) ON DELETE CASCADE,
  INDEX (user_id),
  UNIQUE (user_id,friend_user_id)
);