DELETE FROM process_log;

DROP TABLE statistics;

CREATE TABLE `statistic` (
	`date_time` DATETIME NOT NULL,
	`statistic_name` VARCHAR(50) NOT NULL COLLATE 'utf8mb4_unicode_ci',
	`statistic_value` DECIMAL(10,2) NOT NULL,
	`aggregated_value` DECIMAL(10,2) NOT NULL
)
COLLATE='utf8mb4_unicode_ci'
ENGINE=InnoDB;

ALTER TABLE performer_organization_participation 
	ADD COLUMN service_type VARCHAR(50) NOT NULL DEFAULT 'HEBAMME' AFTER practitioner_display_hash;