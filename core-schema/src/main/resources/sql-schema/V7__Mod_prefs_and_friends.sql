
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
