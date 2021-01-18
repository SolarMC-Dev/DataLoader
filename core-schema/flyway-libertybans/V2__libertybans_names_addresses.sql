
-- Copied from LibertyBans. Used only for jooq-codegen; not present at runtime

CREATE TABLE `libertybans_names` (
`uuid` BINARY(16) NOT NULL,
`name` VARCHAR(16) NOT NULL,
`updated` BIGINT NOT NULL,
PRIMARY KEY (`uuid`, `name`));

CREATE TABLE `libertybans_addresses` (
`uuid` BINARY(16) NOT NULL,
`address` VARBINARY(16) NOT NULL,
`updated` BIGINT NOT NULL,
PRIMARY KEY (`uuid`, `address`));
