
CREATE SEQUENCE public.confirmation_request_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START 1
	CACHE 1
	NO CYCLE;


CREATE SEQUENCE public.insurant_registration_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START 1
	CACHE 1
	NO CYCLE;

CREATE SEQUENCE public.insurant_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START 1
	CACHE 1
	NO CYCLE;

	
-- public.insurant definition

CREATE TABLE insurant (
	id int8 NOT NULL DEFAULT nextval('insurant_seq'::regclass),
	pseudonym varchar(128) NOT NULL,
	user_identifier varchar(255) NULL DEFAULT NULL::character varying,
	metadata text NULL,
	CONSTRAINT insurant_pkey PRIMARY KEY (id),
	CONSTRAINT insurant_un UNIQUE (pseudonym),
	CONSTRAINT user_identifier_un UNIQUE (user_identifier)
);


-- public.performer_organization definition

CREATE TABLE performer_organization (
	ik int4 NOT NULL,
	institution_name_row_1 varchar(255) NULL DEFAULT NULL::character varying,
	institution_name_row_2 varchar(255) NULL DEFAULT NULL::character varying,
	first_name varchar(255) NOT NULL,
	last_name varchar(255) NOT NULL,
	CONSTRAINT performer_organization_pkey PRIMARY KEY (ik)
);


-- public.performer_organization_participation definition

CREATE TABLE performer_organization_participation (
	ik_hash varchar(50) NOT NULL,
	practitioner_display_hash varchar(50) NOT NULL DEFAULT ''::character varying,
	first_request_date_time timestamp NOT NULL,
	latest_request_date_time timestamp NULL,
	CONSTRAINT performer_organization_participation_pkey PRIMARY KEY (ik_hash, practitioner_display_hash)
);


-- public.process_log definition

CREATE TABLE process_log (
	date_time timestamp NOT NULL,
	sender_ik int4 NULL,
	source_software_identifier varchar(255) NULL DEFAULT NULL::character varying,
	charge_item_uuid varchar(36) NULL DEFAULT NULL::character varying,
	state varchar(255) NOT NULL,
	detail varchar(255) NULL DEFAULT NULL::character varying
);


-- public.software definition

CREATE TABLE software (
	ik int4 NOT NULL,
	software_identifier varchar(255) NOT NULL,
	first_request_date_time timestamp NOT NULL,
	latest_request_date_time timestamp NULL,
	CONSTRAINT software_pkey PRIMARY KEY (ik, software_identifier)
);


-- public."statistics" definition

CREATE TABLE "statistics" (
	date_time timestamp NOT NULL,
	"key" varchar(50) NOT NULL,
	value numeric(20, 6) NOT NULL,
	aggregated numeric(20, 6) NOT NULL,
	CONSTRAINT statistics_pkey PRIMARY KEY (date_time, key)
);


-- public.trusted_certificate definition

CREATE TABLE trusted_certificate (
	serial_number numeric(19) NOT NULL,
	ik int4 NOT NULL,
	subject varchar(500) NULL DEFAULT NULL::character varying,
	"data" bytea NOT NULL,
	update_timestamp timestamp NOT NULL,
	CONSTRAINT trusted_certificate_pkey PRIMARY KEY (serial_number)
);
CREATE INDEX ik ON public.trusted_certificate USING btree (ik);
CREATE INDEX update_timestamp ON public.trusted_certificate USING btree (update_timestamp);


-- public.confirmation_request definition

CREATE TABLE confirmation_request (
	id int8 NOT NULL DEFAULT nextval('confirmation_request_seq'::regclass),
	insurant_id int8 NOT NULL,
	encrypted_kvnr varchar(255) NOT NULL DEFAULT ''::character varying,
	state varchar(20) NOT NULL,
	sender_ik int4 NOT NULL,
	request_date_time timestamp NOT NULL,
	response_type varchar(10) NOT NULL,
	software_identifier varchar(255) NOT NULL,
	contact_mail_address varchar(100) NOT NULL,
	callback_url varchar(255) NULL DEFAULT NULL::character varying,
	uuid varchar(50) NOT NULL,
	service_type varchar(50) NOT NULL,
	code varchar(10) NOT NULL,
	occurrence_date_time varchar(50) NULL DEFAULT ''::character varying,
	occurrence_start_date_time varchar(50) NULL DEFAULT NULL::character varying,
	occurrence_end_date_time varchar(50) NULL DEFAULT NULL::character varying,
	performer_organization_ik int4 NOT NULL,
	performer_organization_display varchar(255) NOT NULL,
	performer_practitioner_display varchar(255) NULL DEFAULT NULL::character varying,
	response_date_time timestamp NULL,
	response_document_bundle_xml text NULL,
	kostentraeger_ik int4 NOT NULL,
	CONSTRAINT confirmation_request_pkey PRIMARY KEY (id),
	CONSTRAINT sender_ik_uuid UNIQUE (sender_ik, uuid),
	CONSTRAINT fk_confirmation_request_insurant FOREIGN KEY (insurant_id) REFERENCES insurant(id)
);
CREATE INDEX fk_confirmation_request_insurant ON public.confirmation_request USING btree (insurant_id);
CREATE INDEX performer_ik ON public.confirmation_request USING btree (performer_organization_ik);


-- public.insurant_registration definition

CREATE TABLE insurant_registration (
	id int8 NOT NULL DEFAULT nextval('insurant_registration_seq'::regclass),
	insurant_id int8 NOT NULL,
	registration_date_time timestamp NULL,
	deactivation_date_time timestamp NULL,
	deregistration_date_time timestamp NULL,
	CONSTRAINT insurant_registration_pkey PRIMARY KEY (id),
	CONSTRAINT fk_insurant_registration_insurant FOREIGN KEY (insurant_id) REFERENCES insurant(id)
);
CREATE INDEX fk_insurant_registration_insurant ON public.insurant_registration USING btree (insurant_id);
