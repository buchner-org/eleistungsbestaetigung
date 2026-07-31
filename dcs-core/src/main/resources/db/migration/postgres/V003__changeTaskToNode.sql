DELETE FROM task_counter;
ALTER TABLE task_counter RENAME TO task_node;
ALTER TABLE task_node RENAME COLUMN counter TO node_uuid;
ALTER TABLE task_node ALTER COLUMN node_uuid TYPE varchar(36) USING node_uuid::varchar;
ALTER TABLE task_node ADD last_liveness_datetime timestamp NOT NULL;
