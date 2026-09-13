create TABLE brands (
    id BIGSERIAL PRIMARY KEY ,
    name VARCHAR(100) NOT NULL UNIQUE,
    logo_url  VARCHAR(500),
    description TEXT
);