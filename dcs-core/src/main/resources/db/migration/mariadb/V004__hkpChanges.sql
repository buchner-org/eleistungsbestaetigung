ALTER TABLE confirmation_request
	ADD COLUMN policy_version SMALLINT NOT NULL DEFAULT 0 AFTER callback_url;

ALTER TABLE confirmation_request
	ADD COLUMN performer_practitioner_identifier VARCHAR(255) NULL AFTER `performer_organization_display`;