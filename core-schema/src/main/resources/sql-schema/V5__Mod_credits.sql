
CREATE TABLE credits (
  user_id INT PRIMARY KEY,
  balance NUMERIC(15, 3) NOT NULL,
  FOREIGN KEY (user_id) REFERENCES user_ids (id) ON DELETE CASCADE,
  INDEX balance_index (balance),
  CONSTRAINT nonnegative_balance CHECK (balance >= 0)
);

CREATE VIEW credits_with_names AS
  SELECT credits.user_id, credits.balance, latest_names.username
  FROM credits INNER JOIN latest_names
  ON credits.user_id = latest_names.user_id;

CREATE PROCEDURE credits_withdraw_balance
  (user_identifier INT,
  withdraw_amount NUMERIC(15, 3),
  OUT new_balance NUMERIC(15, 3),
  OUT successful BOOLEAN)
  MODIFIES SQL DATA
  BEGIN
    DECLARE existing_balance NUMERIC(15, 3);
    SELECT balance INTO existing_balance FROM credits WHERE user_id = user_identifier;
    SET new_balance = existing_balance - withdraw_amount;
    IF new_balance >= 0 THEN
      SET successful = TRUE;
      UPDATE credits SET balance = new_balance WHERE user_id = user_identifier;
    ELSE
      SET successful = FALSE;
      -- New balance is always balance after operation
      SET new_balance = existing_balance;
    END IF;
  END;

CREATE FUNCTION credits_deposit_balance
  (user_identifier INT,
  deposit_amount NUMERIC(15, 3))
  RETURNS NUMERIC(15, 3)
  MODIFIES SQL DATA
  BEGIN
    UPDATE credits SET balance = balance + deposit_amount WHERE user_id = user_identifier;
    RETURN (SELECT balance FROM credits WHERE user_id = user_identifier);
  END;
