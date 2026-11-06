-- 1. колонка пока без ограничений, иначе старые строки не пройдут NOT NULL
ALTER TABLE products   ADD COLUMN slug VARCHAR(200);
ALTER TABLE categories ADD COLUMN slug VARCHAR(200);

-- 2. slug из названия: румынские буквы --- латиница, всё кроме a-z и 0-9 --- дефис
UPDATE products SET slug = trim(both '-' from regexp_replace(
        lower(translate(name, 'ăâîșşțţĂÂÎȘŞȚŢ', 'aaisssttAAISSTT')),
        '[^a-z0-9]+', '-', 'g'));

UPDATE categories SET slug = trim(both '-' from regexp_replace(
        lower(translate(name, 'ăâîșşțţĂÂÎȘŞȚŢ', 'aaisssttAAISSTT')),
        '[^a-z0-9]+', '-', 'g'));

-- 3. если slug совпал с более старой записью или вышел пустым — дописываем id
UPDATE products p SET slug = p.slug || '-' || p.id
WHERE p.slug = '' OR EXISTS (SELECT 1 FROM products q WHERE q.slug = p.slug AND q.id < p.id);

UPDATE categories c SET slug = c.slug || '-' || c.id
WHERE c.slug = '' OR EXISTS (SELECT 1 FROM categories q WHERE q.slug = c.slug AND q.id < c.id);

-- 4. теперь можно требовать
ALTER TABLE products   ALTER COLUMN slug SET NOT NULL, ADD CONSTRAINT uq_products_slug UNIQUE (slug);
ALTER TABLE categories ALTER COLUMN slug SET NOT NULL, ADD CONSTRAINT uq_categories_slug UNIQUE (slug);