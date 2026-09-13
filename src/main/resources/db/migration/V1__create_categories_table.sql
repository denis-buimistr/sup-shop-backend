CREATE TABLE categories
(
    id          BIGSERIAL PRIMARY KEY,

    name        VARCHAR(150) NOT NULL UNIQUE,

    description TEXT,

    parent_id   BIGINT,

    CONSTRAINT fk_category_parent
        FOREIGN KEY (parent_id)
            REFERENCES categories (id)

);