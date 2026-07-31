ALTER TABLE insurant DROP CONSTRAINT insurant$user_identifier;

CREATE UNIQUE NONCLUSTERED INDEX insurant$user_identifier
ON insurant(user_identifier)
WHERE user_identifier IS NOT NULL;