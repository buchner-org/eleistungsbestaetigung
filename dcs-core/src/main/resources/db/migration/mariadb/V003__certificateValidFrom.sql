ALTER TABLE trusted_certificate
	ADD COLUMN valid_from DATETIME NOT NULL DEFAULT '1970-01-01' AFTER `subject`;
	
UPDATE trusted_certificate SET update_timestamp = '1970-01-01';