-- SET @@SQL_MODE = CONCAT(@@SQL_MODE, ',ANSI_QUOTES');

CREATE VIEW name_history AS
  SELECT user_ids.id user_id, libertybans_names.name username, libertybans_names.updated updated
  FROM libertybans_names INNER JOIN user_ids
  ON libertybans_names.uuid = user_ids.uuid;

-- SET @@SQL_MODE = CONCAT(@@SQL_MODE, ',ANSI_QUOTES');

CREATE VIEW address_history AS
  SELECT user_ids.id user_id, libertybans_addresses.address address, libertybans_addresses.updated updated
  FROM libertybans_addresses INNER JOIN user_ids
  ON libertybans_addresses.uuid = user_ids.uuid;

CREATE VIEW credits_with_names AS
  SELECT DISTINCT credits.user_id, credits.balance, name_history.username
  FROM credits INNER JOIN name_history
  ON credits.user_id = name_history.user_id
  ORDER BY name_history.updated DESC;
