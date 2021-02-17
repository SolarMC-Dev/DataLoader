
CREATE TABLE credits (
  user_id INT PRIMARY KEY,
  balance NUMERIC(12, 3) NOT NULL CHECK (balance >= 0),
  FOREIGN KEY (user_id) REFERENCES user_ids (id) ON DELETE CASCADE
);

CREATE VIEW credits_with_names AS
  SELECT credits.user_id, credits.balance, latest_names.username
  FROM credits INNER JOIN latest_names
  ON credits.user_id = latest_names.user_id;
