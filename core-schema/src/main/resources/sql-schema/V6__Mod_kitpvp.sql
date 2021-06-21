
CREATE TABLE kitpvp_statistics (
  user_id INT PRIMARY KEY,
  kills INT NOT NULL DEFAULT 0,
  deaths INT NOT NULL DEFAULT 0,
  assists INT NOT NULL DEFAULT 0,
  current_killstreak INT NOT NULL DEFAULT 0,
  highest_killstreak INT NOT NULL DEFAULT 0,
  experience INT NOT NULL DEFAULT 0,
  bounty INT NOT NULL DEFAULT 0,

  FOREIGN KEY (user_id) REFERENCES user_ids (id) ON DELETE CASCADE,
  INDEX kills_index (kills),
  INDEX highest_killstreak_index (highest_killstreak)
);

CREATE TABLE kitpvp_kits_ids (
  kit_id INT AUTO_INCREMENT PRIMARY KEY,
  kit_name VARCHAR(32) NOT NULL,
  UNIQUE INDEX kit_name_uniqueness (kit_name)
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
