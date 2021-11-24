DROP SCHEMA IF EXISTS log CASCADE;

CREATE SCHEMA log;

CREATE TABLE log.button (
       id SERIAL PRIMARY KEY,
       user_id VARCHAR,
       device_id VARCHAR,
       device_platform VARCHAR,
       user_lat DOUBLE PRECISION,
       user_lon DOUBLE PRECISION,
       page VARCHAR,
       button_type VARCHAR,
       route_id VARCHAR,
       trip_id VARCHAR,
       stop_id VARCHAR,
       arrival_time TIMESTAMP,
       event VARCHAR,
       stamp TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE log.focus (
      id SERIAL PRIMARY KEY,
      user_id VARCHAR,
      device_id VARCHAR,
      device_platform VARCHAR,
      user_lat DOUBLE PRECISION,
      user_lon DOUBLE PRECISION,
      event VARCHAR,
      stamp TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE log.status (
       id SERIAL PRIMARY KEY,
       device_id VARCHAR,
       device_platform VARCHAR,
       app_version VARCHAR,
       screen_reader_on BOOLEAN,
       location_available BOOLEAN,
       stamp TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE log.schedule_row (
       id SERIAL PRIMARY KEY,
       device_id VARCHAR,
       total_rows INTEGER,
       num_favorite INTEGER,
       user_lat DOUBLE PRECISION,
       user_lon DOUBLE PRECISION,
       stamp TIMESTAMP NOT NULL DEFAULT NOW()
);
