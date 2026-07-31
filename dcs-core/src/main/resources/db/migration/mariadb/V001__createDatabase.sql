/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

-- Table: confirmation_request
CREATE TABLE IF NOT EXISTS `confirmation_request` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `insurant_id` int(11) NOT NULL,
  `kostentraeger_ik` int(11) NOT NULL,
  `encrypted_kvnr` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `state` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `sender_ik` int(11) NOT NULL,
  `request_date_time` datetime NOT NULL,
  `response_type` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL,
  `software_identifier` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `contact_mail_address` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `callback_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `uuid` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `service_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `code` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL,
  `occurrence_date_time` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `occurrence_start_date_time` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `occurrence_end_date_time` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `performer_organization_ik` int(11) NOT NULL,
  `performer_organization_display` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `performer_practitioner_display` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `response_date_time` timestamp NULL DEFAULT NULL,
  `processing_fail_count` int(11) NOT NULL,
  `response_document_bundle_uuid` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `response_document_bundle_xml` longtext COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `callback_fail_count` int(11) NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `sender_ik_uuid` (`sender_ik`,`uuid`),
  KEY `insurant_id` (`insurant_id`),
  KEY `performer_organization_ik` (`performer_organization_ik`),
  CONSTRAINT `FK_confirmation_request_insurant` FOREIGN KEY (`insurant_id`) REFERENCES `insurant` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: insurant
CREATE TABLE IF NOT EXISTS `insurant` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `pseudonym` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_identifier` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `metadata` longtext COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `pseudonym` (`pseudonym`),
  UNIQUE KEY `user_identifier` (`user_identifier`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: insurant_push_task
CREATE TABLE IF NOT EXISTS `insurant_push_task` (
  `insurant_id` int(11) NOT NULL,
  `request_date_time` datetime NOT NULL,
  `push_date_time` datetime DEFAULT NULL,
  PRIMARY KEY (`insurant_id`),
  CONSTRAINT `FK__insurant` FOREIGN KEY (`insurant_id`) REFERENCES `insurant` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: insurant_registration
CREATE TABLE IF NOT EXISTS `insurant_registration` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `insurant_id` int(11) NOT NULL,
  `registration_date_time` datetime DEFAULT NULL,
  `deactivation_date_time` datetime DEFAULT NULL,
  `deregistration_date_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_insurant_registration_insurant` (`insurant_id`),
  CONSTRAINT `FK_insurant_registration_insurant` FOREIGN KEY (`insurant_id`) REFERENCES `insurant` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: performer_organization
CREATE TABLE IF NOT EXISTS `performer_organization` (
  `ik` int(11) NOT NULL,
  `institution_name_row_1` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `institution_name_row_2` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `first_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `last_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`ik`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: performer_organization_participation
CREATE TABLE IF NOT EXISTS `performer_organization_participation` (
  `ik_hash` varchar(250) COLLATE utf8mb4_unicode_ci NOT NULL,
  `practitioner_display_hash` varchar(250) COLLATE utf8mb4_unicode_ci NOT NULL,
  `first_request_date_time` datetime NOT NULL,
  `latest_request_date_time` datetime DEFAULT NULL,
  PRIMARY KEY (`ik_hash`,`practitioner_display_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: process_log
CREATE TABLE IF NOT EXISTS `process_log` (
  `date_time` datetime NOT NULL,
  `sender_ik` int(11) DEFAULT NULL,
  `source_software_identifier` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `charge_item_uuid` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `state` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `detail` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: software
CREATE TABLE IF NOT EXISTS `software` (
  `ik` int(11) NOT NULL,
  `software_identifier` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `first_request_date_time` datetime NOT NULL,
  `latest_request_date_time` datetime DEFAULT NULL,
  PRIMARY KEY (`ik`,`software_identifier`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: statistics
CREATE TABLE IF NOT EXISTS `statistics` (
  `date_time` datetime NOT NULL,
  `key` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `value` decimal(10,2) NOT NULL,
  `aggregated` decimal(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: task_node
CREATE TABLE IF NOT EXISTS `task_node` (
  `task_id` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `node_uuid` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
  `last_liveness_datetime` datetime NOT NULL,
  PRIMARY KEY (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: trusted_certificate
CREATE TABLE IF NOT EXISTS `trusted_certificate` (
  `serial_number` decimal(19,0) NOT NULL,
  `ik` int(11) NOT NULL,
  `subject` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `data` longblob NOT NULL,
  `update_timestamp` datetime NOT NULL,
  PRIMARY KEY (`serial_number`),
  KEY `ik` (`ik`),
  KEY `update_timestamp` (`update_timestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
