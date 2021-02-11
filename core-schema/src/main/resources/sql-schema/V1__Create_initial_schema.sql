
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

CREATE TABLE clans_clan_info (
  clan_id INT NOT NULL AUTO_INCREMENT UNIQUE,
  clan_name VARCHAR(32) NOT NULL,
  clan_leader INT NOT NULL,
  clan_kills INT NOT NULL,
  clan_deaths INT NOT NULL,
  clan_assists INT NOT NULL,
  PRIMARY KEY (clan_id)
);

CREATE TABLE clans_clan_membership (
  user_id INT NOT NULL,
  clan_id INT NOT NULL,
  FOREIGN KEY (clan_id) REFERENCES clans_clan_info (clan_id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES user_ids (id) ON DELETE CASCADE,
  PRIMARY KEY (user_id),
  INDEX (clan_id)
);

CREATE TABLE clans_clan_alliances (
  clan_id INT NOT NULL,
  ally_id INT NOT NULL,
  FOREIGN KEY (clan_id) REFERENCES clans_clan_info (clan_id) ON DELETE CASCADE,
  FOREIGN KEY (ally_id) REFERENCES clans_clan_info (clan_id) ON DELETE CASCADE,
  PRIMARY KEY (clan_id),
  INDEX (ally_id)
);

CREATE TABLE clans_clan_enemies (
  clan_id INT NOT NULL,
  enemy_id INT NOT NULL,
  FOREIGN KEY (clan_id) REFERENCES clans_clan_info (clan_id) ON DELETE CASCADE,
  FOREIGN KEY (enemy_id) REFERENCES clans_clan_info (clan_id) ON DELETE CASCADE,
  PRIMARY KEY (clan_id,enemy_id),
  INDEX (enemy_id)
);