--DROP TABLE users;

--DROP TABLE items;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(512) NOT NULL,
  CONSTRAINT pk_user PRIMARY KEY (id),
  CONSTRAINT UQ_USER_EMAIL UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS items (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT,
    name VARCHAR(255),
    description VARCHAR(1000),
    available BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_items_to_users FOREIGN KEY(user_id) REFERENCES users(id)
);
