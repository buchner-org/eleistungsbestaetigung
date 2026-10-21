DELETE FROM trusted_certificate;

ALTER TABLE trusted_certificate
	ADD COLUMN valid_from timestamp NOT NULL;
