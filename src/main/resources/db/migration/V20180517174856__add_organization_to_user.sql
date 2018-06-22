ALTER TABLE users_data ADD COLUMN IF NOT EXISTS organization VARCHAR(100) NOT NULL DEFAULT '';
CREATE OR REPLACE VIEW users AS
  SELECT *
  FROM users_data u
  WHERE EXISTS(
      SELECT 1
      FROM tenant_dependent_users_view tu
      WHERE tu.user_id = u.id AND tu.tenant_id = current_setting('app.tenant_id')::BIGINT
  );
