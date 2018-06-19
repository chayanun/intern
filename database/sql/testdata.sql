START TRANSACTION;

set search_path = intern;

INSERT INTO contact_service (service_id, service_name)
VALUES
(1, 'Request a Demo')
,(2, 'Sales Inquiry')
,(3, 'Customer Support');

SELECT setval(pg_get_serial_sequence('contact_service', 'service_id'), COALESCE((SELECT MAX(service_id)+1 FROM contact_service), 1), false);

-- Post-data save --
COMMIT;