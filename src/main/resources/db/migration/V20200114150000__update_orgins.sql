UPDATE users_data SET
origin = 'JDBC'
WHERE origin = 'NATIVE';

update system_settings set value ='db'
where name = 'portal.authMethod' and value = 'NATIVE';

update system_settings set name ='security.method'
where name = 'portal.authMethod';