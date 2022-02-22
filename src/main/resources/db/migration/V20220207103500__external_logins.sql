CREATE SEQUENCE external_logins_id_seq MINVALUE 1;

CREATE TABLE external_logins
(
    id BIGINT PRIMARY KEY NOT NULL,
    provider VARCHAR(255) NOT NULL,
    sub VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    created TIMESTAMP NOT NULL,
    user_id BIGINT NULL
);

ALTER TABLE external_logins ADD CONSTRAINT external_logins__user_id_fkey FOREIGN KEY (user_id) REFERENCES users_data (id) ON DELETE CASCADE ON UPDATE CASCADE;
