CREATE OR REPLACE VIEW users(email, password, enabled, id, firstname, middlename, lastname, last_password_reset, created, updated, professional_type_id, registration_code, affiliation, personal_summary, contact_info, email_confirmed, phone, mobile, address1, address2, city, state_province_id, zip_code, country_id, origin, username, active_tenant_id, organization, department, deleted) as
SELECT u.email,
       u.password,
       u.enabled,
       u.id,
       u.firstname,
       u.middlename,
       u.lastname,
       u.last_password_reset,
       u.created,
       u.updated,
       u.professional_type_id,
       u.registration_code,
       u.affiliation,
       u.personal_summary,
       u.contact_info,
       u.email_confirmed,
       u.phone,
       u.mobile,
       u.address1,
       u.address2,
       u.city,
       u.state_province_id,
       u.zip_code,
       u.country_id,
       u.origin,
       u.username,
       u.active_tenant_id,
       u.organization,
       u.department,
       u.deleted
FROM users_data u
WHERE (EXISTS(SELECT 1
              FROM tenant_dependent_users_view tu
              WHERE tu.user_id = u.id
                AND tu.tenant_id = current_setting('app.tenant_id'::text)::bigint));