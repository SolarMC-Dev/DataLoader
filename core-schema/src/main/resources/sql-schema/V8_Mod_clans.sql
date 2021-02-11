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