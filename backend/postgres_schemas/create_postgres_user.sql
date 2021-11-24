CREATE USER postgres WITH PASSWORD 'f22f22';

GRANT USAGE ON SCHEMA log TO postgres;

GRANT SELECT ON ALL TABLES IN SCHEMA log TO postgres;
GRANT INSERT ON ALL TABLES IN SCHEMA log TO postgres;
GRANT UPDATE ON ALL TABLES IN SCHEMA log TO postgres;

GRANT USAGE ON ALL SEQUENCES IN SCHEMA log TO postgres;
GRANT SELECT ON ALL SEQUENCES IN SCHEMA log TO postgres;

GRANT USAGE ON SCHEMA user_data TO postgres;

GRANT SELECT ON ALL TABLES IN SCHEMA user_data TO postgres;
GRANT INSERT ON ALL TABLES IN SCHEMA user_data TO postgres;
GRANT UPDATE ON ALL TABLES IN SCHEMA user_data TO postgres;

GRANT USAGE ON ALL SEQUENCES IN SCHEMA user_data TO postgres;
GRANT SELECT ON ALL SEQUENCES IN SCHEMA user_data TO postgres;

GRANT USAGE ON SCHEMA exp TO postgres;

GRANT SELECT ON ALL TABLES IN SCHEMA exp TO postgres;
GRANT INSERT ON ALL TABLES IN SCHEMA exp TO postgres;
GRANT UPDATE ON ALL TABLES IN SCHEMA exp TO postgres;

GRANT USAGE ON ALL SEQUENCES IN SCHEMA exp TO postgres;
GRANT SELECT ON ALL SEQUENCES IN SCHEMA exp TO postgres;
