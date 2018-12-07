ALTER TABLE users_data ALTER COLUMN organization SET DEFAULT 'Not available';

ALTER TABLE users_data ADD COLUMN IF NOT EXISTS department VARCHAR;

UPDATE users_data SET organization = 'Not available' WHERE organization = '' OR organization IS NULL;

CREATE OR REPLACE VIEW users AS
	SELECT *
	FROM users_data u
	WHERE EXISTS(
			SELECT 1
			FROM tenant_dependent_users_view tu
			WHERE tu.user_id = u.id AND tu.tenant_id = current_setting('app.tenant_id')::BIGINT
	);