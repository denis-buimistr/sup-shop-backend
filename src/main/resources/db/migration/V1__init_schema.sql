-- вся схема сразу в итоговом виде. до прода тут было 16 миграций, перед деплоем склеил их в одну

-- каталог

CREATE TABLE categories (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(150) NOT NULL UNIQUE,
    slug        VARCHAR(200) NOT NULL,
    description TEXT,
    parent_id   BIGINT,
    CONSTRAINT uq_categories_slug UNIQUE (slug),
    CONSTRAINT fk_category_parent FOREIGN KEY (parent_id) REFERENCES categories (id)
);

CREATE TABLE brands (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    logo_url    VARCHAR(500),
    description TEXT
);

-- «Sale» это не категория, а товары, у которых compare_at_price больше price
CREATE TABLE products (
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(200)   NOT NULL,
    slug             VARCHAR(200)   NOT NULL,
    description      TEXT,
    price            NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
    compare_at_price NUMERIC(10, 2),
    quantity         INTEGER        NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    brand_id         BIGINT REFERENCES brands (id),
    category_id      BIGINT REFERENCES categories (id),
    board_type       VARCHAR(20),
    sku              VARCHAR(64) UNIQUE,
    status           VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE',
    created_at       TIMESTAMP      NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP      NOT NULL DEFAULT now(),
    CONSTRAINT uq_products_slug UNIQUE (slug),
    CONSTRAINT chk_products_compare_at_price CHECK (compare_at_price IS NULL OR compare_at_price > price)
);

CREATE TABLE product_images (
    id         BIGSERIAL PRIMARY KEY,
    product_id BIGINT       NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    image_url  VARCHAR(500) NOT NULL,
    is_primary BOOLEAN      NOT NULL DEFAULT FALSE,
    sort_order INTEGER      NOT NULL DEFAULT 0
);

CREATE TABLE product_attributes (
    id              BIGSERIAL PRIMARY KEY,
    product_id      BIGINT       NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    attribute_name  VARCHAR(100) NOT NULL,
    attribute_value VARCHAR(255) NOT NULL
);

-- переводы на румынский и русский. английский лежит в основных таблицах, он же запасной, если перевода нет

CREATE TABLE category_translations (
    category_id BIGINT       NOT NULL REFERENCES categories (id) ON DELETE CASCADE,
    lang        VARCHAR(2)   NOT NULL CHECK (lang IN ('ro', 'ru')),
    name        VARCHAR(150) NOT NULL,
    description TEXT,
    PRIMARY KEY (category_id, lang)
);

-- название товара обычно модель (Starboard All Star), его не перевожу, поэтому name может быть пустым
CREATE TABLE product_translations (
    product_id  BIGINT     NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    lang        VARCHAR(2) NOT NULL CHECK (lang IN ('ro', 'ru')),
    name        VARCHAR(200),
    description TEXT,
    PRIMARY KEY (product_id, lang)
);

-- value пустой, если значение и так понятно на любом языке (цифры, Carbon Reflex)
CREATE TABLE product_attribute_translations (
    attribute_id BIGINT       NOT NULL REFERENCES product_attributes (id) ON DELETE CASCADE,
    lang         VARCHAR(2)   NOT NULL CHECK (lang IN ('ro', 'ru')),
    name         VARCHAR(100) NOT NULL,
    value        VARCHAR(255),
    PRIMARY KEY (attribute_id, lang)
);

-- пользователи

CREATE TABLE roles (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(250) NOT NULL UNIQUE,
    password_hash VARCHAR(250) NOT NULL,
    full_name     VARCHAR(150),
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles (id) ON DELETE CASCADE
);

-- роли это справочник, без них приложение не работает. админа создаёт AdminInitializer из переменных окружения
INSERT INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_ADMIN');

-- избранное: один товар у юзера максимум один раз
CREATE TABLE wishlist_items (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT    NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    product_id BIGINT    NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (user_id, product_id)
);

-- корзина и заказы

CREATE TABLE carts (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT    NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE cart_items (
    id         BIGSERIAL PRIMARY KEY,
    cart_id    BIGINT  NOT NULL REFERENCES carts (id) ON DELETE CASCADE,
    product_id BIGINT  NOT NULL REFERENCES products (id),
    quantity   INTEGER NOT NULL CHECK (quantity > 0),
    UNIQUE (cart_id, product_id)
);

CREATE TABLE orders (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT         NOT NULL REFERENCES users (id),
    status           VARCHAR(20)    NOT NULL DEFAULT 'NEW',
    first_name       VARCHAR(100),
    last_name        VARCHAR(100),
    phone            VARCHAR(30),
    email            VARCHAR(250),
    delivery_method  VARCHAR(30),
    city             VARCHAR(100),
    shipping_address VARCHAR(500),
    delivery_price   NUMERIC(10, 2) NOT NULL DEFAULT 0,
    payment_method   VARCHAR(30),
    comment          VARCHAR(500),
    total_price      NUMERIC(10, 2) NOT NULL DEFAULT 0,
    created_at       TIMESTAMP      NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP      NOT NULL DEFAULT now()
);

-- название и цену копирую в заказ, чтобы старые заказы не менялись, когда меняется товар
CREATE TABLE order_items (
    id                BIGSERIAL PRIMARY KEY,
    order_id          BIGINT         NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id        BIGINT         NOT NULL REFERENCES products (id),
    product_name      VARCHAR(200)   NOT NULL,
    quantity          INTEGER        NOT NULL CHECK (quantity > 0),
    price_at_purchase NUMERIC(10, 2) NOT NULL
);

CREATE INDEX idx_orders_user ON orders (user_id);
CREATE INDEX idx_cart_items_cart ON cart_items (cart_id);
CREATE INDEX idx_order_items_order ON order_items (order_id);
