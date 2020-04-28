CREATE DATABASE postgres_drop;

\connect  postgres_drop;

CREATE TABLE public.person(
   id serial PRIMARY KEY,
   email VARCHAR (50) UNIQUE NOT NULL,
   first_name VARCHAR (50) NOT NULL,
   last_name VARCHAR (50) NOT NULL
);