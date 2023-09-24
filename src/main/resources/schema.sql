DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS items;
DROP TABLE IF EXISTS users;



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

CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    booker_id BIGINT,
    item_id BIGINT,
    start_date timestamp without time zone,
    end_date timestamp without time zone,
    status VARCHAR(50),
    CONSTRAINT fk_bookings_to_items FOREIGN KEY(item_id) REFERENCES items(id),
    CONSTRAINT fk_bookings_to_bookers FOREIGN KEY(booker_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS comments (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    item_id BIGINT,
    author_id BIGINT,
    text VARCHAR(1000),
    created timestamp without time zone,
    CONSTRAINT fk_comments_to_items FOREIGN KEY(item_id) REFERENCES items(id),
    CONSTRAINT fk_comments_to_bookers FOREIGN KEY(author_id) REFERENCES users(id)
);
