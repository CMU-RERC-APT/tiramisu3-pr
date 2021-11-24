DROP SCHEMA IF EXISTS exp CASCADE;

CREATE SCHEMA exp;

CREATE TABLE exp.description (
       id SERIAL PRIMARY KEY,
       name VARCHAR,
       exp_descrip VARCHAR,
       cond_descrip VARCHAR,
       start_date TIMESTAMP,
       end_date TIMESTAMP
);

CREATE TABLE exp.user_condition (
       id SERIAL PRIMARY KEY,
       device_id VARCHAR,
       experiment_id INTEGER REFERENCES exp.description,
       condition_num INTEGER,
       stamp TIMESTAMP DEFAULT NOW()
);

CREATE TABLE exp.user_pair (
       id SERIAL PRIMARY KEY,
       device_id1 VARCHAR,
       device_id2 VARCHAR,
       stamp TIMESTAMP DEFAULT NOW()
);
