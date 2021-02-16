
CREATE VIEW name_history AS
  SELECT user_ids.id user_id, user_ids.uuid uuid, libertybans_names.name username, libertybans_names.updated updated
  FROM libertybans_names INNER JOIN user_ids
  ON libertybans_names.uuid = user_ids.uuid;

CREATE VIEW address_history AS
  SELECT user_ids.id user_id, user_ids.uuid uuid, libertybans_addresses.address address, libertybans_addresses.updated updated
  FROM libertybans_addresses INNER JOIN user_ids
  ON libertybans_addresses.uuid = user_ids.uuid;

-- Exclusive outer join to return the most recent name of each player

CREATE VIEW latest_names AS
  SELECT nh1.user_id, nh1.uuid, nh1.username, nh1.updated
  FROM name_history nh1
  LEFT JOIN name_history nh2
  ON nh1.user_id = nh2.user_id
  AND nh1.updated < nh2.updated
  WHERE nh2.user_id IS NULL;
