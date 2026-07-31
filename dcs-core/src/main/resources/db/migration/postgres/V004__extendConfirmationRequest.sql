UPDATE confirmation_request SET response_document_bundle_xml = NULL;

ALTER TABLE confirmation_request ADD response_document_bundle_uuid varchar(36) NULL;
ALTER TABLE confirmation_request ADD processing_fail_count int4 NOT NULL;
ALTER TABLE confirmation_request ADD callback_fail_count int4 NOT NULL;


CREATE TABLE public.insurant_push_task (
	insurant_id int8 NOT NULL,
	request_date_time timestamp NOT NULL,
	push_date_time timestamp NULL
);
