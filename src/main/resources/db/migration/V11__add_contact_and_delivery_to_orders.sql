ALTER TABLE orders
    ADD COLUMN first_name      VARCHAR(100),
    ADD COLUMN last_name       VARCHAR(100),
    ADD COLUMN phone           VARCHAR(30),
    ADD COLUMN email           VARCHAR(250),
    ADD COLUMN delivery_method VARCHAR(30),
    ADD COLUMN city            VARCHAR(100),
    ADD COLUMN delivery_price  NUMERIC(10, 2) NOT NULL DEFAULT 0,
    ADD COLUMN payment_method  VARCHAR(30),
    ADD COLUMN comment         VARCHAR(500);