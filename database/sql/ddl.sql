CREATE TABLE contact_service (
  service_id SERIAL PRIMARY KEY,
  service_name character varying(255) NOT NULL
);

CREATE TABLE contact_data (
  contact_id SERIAL PRIMARY KEY,
  contact_service_id integer NOT NULL,
  contact_name character varying(255) NOT NULL,
  contact_email character varying(100) NOT NULL,
  contact_phone character varying(50) DEFAULT NULL,
  contact_message text DEFAULT NULL,
  created_date timestamp with time zone NOT NULL
);

ALTER TABLE "contact_data"
ADD CONSTRAINT "contact_service_fk" FOREIGN KEY ("contact_service_id") REFERENCES "contact_service" ("service_id");