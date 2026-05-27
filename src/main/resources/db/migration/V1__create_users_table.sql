CREATE TABLE users
(
    id serial PRIMARY KEY,
    user_id uuid NOT NULL DEFAULT gen_random_uuid(),
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255),

    CONSTRAINT users_user_id_key UNIQUE (user_id),
    CONSTRAINT users_username_key UNIQUE (username),
    CONSTRAINT users_email_key UNIQUE (email)
);