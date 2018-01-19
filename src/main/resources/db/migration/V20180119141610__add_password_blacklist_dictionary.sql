INSERT INTO system_settings (group_id, label, name, value, type)
VALUES ((SELECT id
         FROM system_settings_groups
         WHERE name = 'auth'), 'Password Blacklist',
        'arachne.passwordBlacklist', NULL, 'text'
);