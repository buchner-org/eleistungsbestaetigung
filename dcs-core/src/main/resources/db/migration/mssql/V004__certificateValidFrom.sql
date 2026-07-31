ALTER TABLE trusted_certificate ADD valid_from datetime2(0) DEFAULT '1970-01-01' NOT NULL;

UPDATE trusted_certificate SET update_timestamp = '1970-01-01';