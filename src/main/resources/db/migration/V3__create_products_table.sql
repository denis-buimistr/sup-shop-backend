CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY ,
    name VARCHAR(200) NOT NULL ,
    description TEXT ,
    price NUMERIC(10,2) NOT NULL CHECK ( price >= 0 ),
    quantity INTEGER NOT NULL DEFAULT 0 CHECK ( quantity >= 0 ),
    brand_id BIGINT REFERENCES brands(id),
    category_id BIGINT references categories(id),
    board_type VARCHAR(20),
    sku VARCHAR(64) UNIQUE ,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);