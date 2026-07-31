CREATE TABLE task_counter (
	task_id varchar(20) NOT NULL,
	counter int8 NOT NULL,
	CONSTRAINT task_counter_pk PRIMARY KEY (task_id)
);

INSERT INTO task_counter (task_id, counter) VALUES('generic', 1);
