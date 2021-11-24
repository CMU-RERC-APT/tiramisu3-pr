DROP SCHEMA IF EXISTS user_data CASCADE;

CREATE SCHEMA user_data;

CREATE TABLE user_data.route (
       id SERIAL PRIMARY KEY,
       user_id VARCHAR,
       device_id VARCHAR,
       user_lat DOUBLE PRECISION,
       user_lon DOUBLE PRECISION,
       route_id VARCHAR,
       route_short_name VARCHAR,
       agency_id VARCHAR,
       event VARCHAR,
       pair_id INTEGER,
       stamp TIMESTAMP NOT NULL DEFAULT NOW()

);

CREATE TABLE user_data.stop (
       id SERIAL PRIMARY KEY,
       user_id VARCHAR,
       device_id VARCHAR,
       user_lat DOUBLE PRECISION,
       user_lon DOUBLE PRECISION,
       stop_id VARCHAR,
       stop_name VARCHAR,
       stop_lat DOUBLE PRECISION,
       stop_lon DOUBLE PRECISION,
       agency_id VARCHAR,
       event VARCHAR,
       pair_id INTEGER,
       stamp TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE user_data.alarm (
       id SERIAL PRIMARY KEY,
       event VARCHAR,
       pair_id INTEGER,
       device_id VARCHAR, 
       registration_id VARCHAR,
       device_platform VARCHAR,
       user_id VARCHAR, 
       user_lon DOUBLE PRECISION,
       user_lat DOUBLE PRECISION,
       route_name VARCHAR,
       trip_headsign VARCHAR,
       trip_id VARCHAR, 
       stop_id VARCHAR, 
       stop_name VARCHAR,
       service_date TIMESTAMP,
       stamp TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE user_data.search (
       id SERIAL PRIMARY KEY,
       user_id VARCHAR,
       device_id VARCHAR,
       query VARCHAR,
       user_lat DOUBLE PRECISION,
       user_lon DOUBLE PRECISION,
       place_lat DOUBLE PRECISION,
       place_lon DOUBLE PRECISION,
       is_recent BOOLEAN,
       stamp TIMESTAMP NOT NULL DEFAULT NOW()
);


CREATE TABLE user_data.settings (
       id SERIAL PRIMARY KEY,
       user_lat DOUBLE PRECISION,
       user_lon DOUBLE PRECISION,
       user_id VARCHAR,
       device_id VARCHAR,
       show_map_on_start BOOLEAN,
       need_seat BOOLEAN,
       is_blind BOOLEAN,
       is_deaf BOOLEAN,
       walker_user BOOLEAN,
       cog_disab BOOLEAN,
       scooter_user BOOLEAN,
       other BOOLEAN,
       stamp TIMESTAMP NOT NULL DEFAULT NOW()
);
