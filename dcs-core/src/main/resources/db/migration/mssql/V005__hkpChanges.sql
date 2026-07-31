ALTER TABLE confirmation_request
	ADD policy_version SMALLINT NOT NULL DEFAULT 0;

ALTER TABLE confirmation_request
	ADD performer_practitioner_identifier VARCHAR(255) NULL;
