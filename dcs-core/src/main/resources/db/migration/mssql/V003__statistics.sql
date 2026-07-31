DELETE FROM process_log;

DROP TABLE [statistics];

CREATE TABLE statistic (
	date_time datetime2(0) NOT NULL,
	statistic_name nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	statistic_value decimal(10,2) NOT NULL,
	aggregated_value decimal(10,2) NOT NULL
);

ALTER TABLE performer_organization_participation ADD service_type varchar(50) DEFAULT 'HEBAMME' NOT NULL;
