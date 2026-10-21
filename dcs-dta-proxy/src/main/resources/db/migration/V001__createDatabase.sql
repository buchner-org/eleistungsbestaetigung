CREATE TABLE trusted_certificate (
	serial_number numeric(19,0) NOT NULL,
	ik int NOT NULL,
	subject varchar(500),
	data VARBINARY(10000) NOT NULL,
	update_timestamp timestamp NOT NULL,
	PRIMARY KEY (serial_number)
);

CREATE INDEX ik ON trusted_certificate(ik);
CREATE INDEX update_timestamp ON trusted_certificate(update_timestamp);