-- digital_confirmation_service.dbo.insurant definition

-- Drop table

-- DROP TABLE digital_confirmation_service.dbo.insurant;

CREATE TABLE insurant (
	id bigint IDENTITY(1,1) NOT NULL,
	pseudonym nvarchar(128) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	user_identifier nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS DEFAULT NULL NULL,
	metadata nvarchar(MAX) COLLATE SQL_Latin1_General_CP1_CI_AS DEFAULT NULL NULL,
	CONSTRAINT PK_insurant_id PRIMARY KEY (id),
	CONSTRAINT insurant$pseudonym UNIQUE (pseudonym),
	CONSTRAINT insurant$user_identifier UNIQUE (user_identifier)
);


-- digital_confirmation_service.dbo.performer_organization definition

-- Drop table

-- DROP TABLE digital_confirmation_service.dbo.performer_organization;

CREATE TABLE performer_organization (
	ik int NOT NULL,
	institution_name_row_1 nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS DEFAULT NULL NULL,
	institution_name_row_2 nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS DEFAULT NULL NULL,
	first_name nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	last_name nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	CONSTRAINT PK_performer_organization_ik PRIMARY KEY (ik)
);


-- digital_confirmation_service.dbo.performer_organization_participation definition

-- Drop table

-- DROP TABLE digital_confirmation_service.dbo.performer_organization_participation;

CREATE TABLE performer_organization_participation (
	ik_hash nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	practitioner_display_hash nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	first_request_date_time datetime2(0) NOT NULL,
	latest_request_date_time datetime2(0) DEFAULT NULL NULL,
	CONSTRAINT PK_performer_organization_participation_ik_hash PRIMARY KEY (ik_hash,practitioner_display_hash)
);


-- digital_confirmation_service.dbo.process_log definition

-- Drop table

-- DROP TABLE digital_confirmation_service.dbo.process_log;

CREATE TABLE process_log (
	date_time datetime2(0) NOT NULL,
	sender_ik int DEFAULT NULL NULL,
	source_software_identifier nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS DEFAULT NULL NULL,
	charge_item_uuid nvarchar(36) COLLATE SQL_Latin1_General_CP1_CI_AS DEFAULT NULL NULL,
	state nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	detail nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS DEFAULT NULL NULL
);


-- digital_confirmation_service.dbo.software definition

-- Drop table

-- DROP TABLE digital_confirmation_service.dbo.software;

CREATE TABLE software (
	ik int NOT NULL,
	software_identifier nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	first_request_date_time datetime2(0) NOT NULL,
	latest_request_date_time datetime2(0) DEFAULT NULL NULL,
	CONSTRAINT PK_software_ik PRIMARY KEY (ik,software_identifier)
);


-- digital_confirmation_service.dbo.[statistics] definition

-- Drop table

-- DROP TABLE digital_confirmation_service.dbo.[statistics];

CREATE TABLE [statistics] (
	date_time datetime2(0) NOT NULL,
	[key] nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	value decimal(10,2) NOT NULL,
	aggregated decimal(10,2) NOT NULL
);


-- digital_confirmation_service.dbo.task_node definition

-- Drop table

-- DROP TABLE digital_confirmation_service.dbo.task_node;

CREATE TABLE task_node (
	task_id nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	node_uuid nvarchar(36) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	last_liveness_datetime datetime2(0) NOT NULL,
	CONSTRAINT PK_task_node_task_id PRIMARY KEY (task_id)
);


-- digital_confirmation_service.dbo.trusted_certificate definition

-- Drop table

-- DROP TABLE digital_confirmation_service.dbo.trusted_certificate;

CREATE TABLE trusted_certificate (
	serial_number decimal(19,0) NOT NULL,
	ik int NOT NULL,
	subject nvarchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS DEFAULT NULL NULL,
	[data] varbinary(MAX) NOT NULL,
	update_timestamp datetime2(0) NOT NULL,
	CONSTRAINT PK_trusted_certificate_serial_number PRIMARY KEY (serial_number)
);
 CREATE NONCLUSTERED INDEX ik ON dbo.trusted_certificate (  ik ASC  )  
	 WITH (  PAD_INDEX = OFF ,FILLFACTOR = 100  ,SORT_IN_TEMPDB = OFF , IGNORE_DUP_KEY = OFF , STATISTICS_NORECOMPUTE = OFF , ONLINE = OFF , ALLOW_ROW_LOCKS = ON , ALLOW_PAGE_LOCKS = ON  )
	 ON [PRIMARY ] ;
 CREATE NONCLUSTERED INDEX update_timestamp ON dbo.trusted_certificate (  update_timestamp ASC  )  
	 WITH (  PAD_INDEX = OFF ,FILLFACTOR = 100  ,SORT_IN_TEMPDB = OFF , IGNORE_DUP_KEY = OFF , STATISTICS_NORECOMPUTE = OFF , ONLINE = OFF , ALLOW_ROW_LOCKS = ON , ALLOW_PAGE_LOCKS = ON  )
	 ON [PRIMARY ] ;


-- digital_confirmation_service.dbo.confirmation_request definition

-- Drop table

-- DROP TABLE digital_confirmation_service.dbo.confirmation_request;

CREATE TABLE confirmation_request (
	id bigint IDENTITY(1,1) NOT NULL,
	insurant_id bigint NOT NULL,
	kostentraeger_ik int NOT NULL,
	encrypted_kvnr nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	state nvarchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	sender_ik int NOT NULL,
	request_date_time datetime2(0) NOT NULL,
	response_type nvarchar(10) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	software_identifier nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	contact_mail_address nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	callback_url nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS DEFAULT NULL NULL,
	uuid nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	service_type nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	code nvarchar(10) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	occurrence_date_time nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS DEFAULT NULL NULL,
	occurrence_start_date_time nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS DEFAULT NULL NULL,
	occurrence_end_date_time nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS DEFAULT NULL NULL,
	performer_organization_ik int NOT NULL,
	performer_organization_display nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	performer_practitioner_display nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS DEFAULT NULL NULL,
	response_date_time datetime DEFAULT NULL NULL,
	processing_fail_count int NULL,
	response_document_bundle_uuid nvarchar(36) COLLATE SQL_Latin1_General_CP1_CI_AS DEFAULT NULL NULL,
	response_document_bundle_xml nvarchar(MAX) COLLATE SQL_Latin1_General_CP1_CI_AS DEFAULT NULL NULL,
	callback_fail_count int NULL,
	CONSTRAINT PK_confirmation_request_id PRIMARY KEY (id),
	CONSTRAINT confirmation_request$sender_ik_uuid UNIQUE (sender_ik,uuid),
	CONSTRAINT confirmation_request$FK_confirmation_request_insurant FOREIGN KEY (insurant_id) REFERENCES insurant(id)
);
 CREATE NONCLUSTERED INDEX insurant_id ON dbo.confirmation_request (  insurant_id ASC  )  
	 WITH (  PAD_INDEX = OFF ,FILLFACTOR = 100  ,SORT_IN_TEMPDB = OFF , IGNORE_DUP_KEY = OFF , STATISTICS_NORECOMPUTE = OFF , ONLINE = OFF , ALLOW_ROW_LOCKS = ON , ALLOW_PAGE_LOCKS = ON  )
	 ON [PRIMARY ] ;
 CREATE NONCLUSTERED INDEX performer_organization_ik ON dbo.confirmation_request (  performer_organization_ik ASC  )  
	 WITH (  PAD_INDEX = OFF ,FILLFACTOR = 100  ,SORT_IN_TEMPDB = OFF , IGNORE_DUP_KEY = OFF , STATISTICS_NORECOMPUTE = OFF , ONLINE = OFF , ALLOW_ROW_LOCKS = ON , ALLOW_PAGE_LOCKS = ON  )
	 ON [PRIMARY ] ;


-- digital_confirmation_service.dbo.insurant_registration definition

-- Drop table

-- DROP TABLE digital_confirmation_service.dbo.insurant_registration;

CREATE TABLE insurant_registration (
	id bigint IDENTITY(1,1) NOT NULL,
	insurant_id bigint NOT NULL,
	registration_date_time datetime2(0) DEFAULT NULL NULL,
	deactivation_date_time datetime2(0) DEFAULT NULL NULL,
	deregistration_date_time datetime2(0) DEFAULT NULL NULL,
	CONSTRAINT PK_insurant_registration_id PRIMARY KEY (id),
	CONSTRAINT insurant_registration$FK_insurant_registration_insurant FOREIGN KEY (insurant_id) REFERENCES insurant(id)
);
 CREATE NONCLUSTERED INDEX FK_insurant_registration_insurant ON dbo.insurant_registration (  insurant_id ASC  )  
	 WITH (  PAD_INDEX = OFF ,FILLFACTOR = 100  ,SORT_IN_TEMPDB = OFF , IGNORE_DUP_KEY = OFF , STATISTICS_NORECOMPUTE = OFF , ONLINE = OFF , ALLOW_ROW_LOCKS = ON , ALLOW_PAGE_LOCKS = ON  )
	 ON [PRIMARY ] ;


-- digital_confirmation_service.dbo.insurant_push_task definition

-- Drop table

-- DROP TABLE digital_confirmation_service.dbo.insurant_push_task;

CREATE TABLE insurant_push_task (
	insurant_id bigint NOT NULL,
	request_date_time datetime2(0) NOT NULL,
	push_date_time datetime2(0) NULL,
	CONSTRAINT insurant_push_task_PK PRIMARY KEY (insurant_id),
	CONSTRAINT insurant_push_task_FK FOREIGN KEY (insurant_id) REFERENCES insurant(id)
);