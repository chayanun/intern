START TRANSACTION;

set search_path = intern;

INSERT INTO contact_service (service_id, service_name)
VALUES
(1, 'Request a Demo')
,(2, 'Sales Inquiry')
,(3, 'Customer Support');

INSERT INTO user_login (user_id, user_name, password, user_role)
VALUES (1, 'admin', '1000:67460a5e34490b0fe6d9fb8a7af6bf1618afb39eb07d149e:ea5feed9924e2f3cdaaa4f90ffc5736534039a4a5aadd570', 2);

SELECT setval(pg_get_serial_sequence('contact_service', 'service_id'), COALESCE((SELECT MAX(service_id)+1 FROM contact_service), 1), false);
SELECT setval(pg_get_serial_sequence('user_login', 'user_id'), COALESCE((SELECT MAX(user_id)+1 FROM user_login), 1), false);

-- Post-data save --
COMMIT;