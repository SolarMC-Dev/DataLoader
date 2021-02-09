
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
