INSERT INTO brands (name, description) VALUES
                                           ('Starboard', 'Innovation quality since 1994'),
                                           ('Red Paddle Co', 'The original inflatable SUP brand'),
                                           ('SIC Maui', 'Performance watercraft from Maui'),
                                           ('Fanatic', 'German engineering for water sports')
    ON CONFLICT (name) DO NOTHING;

INSERT INTO categories (name, description, parent_id) VALUES
    ('SUP', 'Stand up paddleboards', NULL)
    ON CONFLICT (name) DO NOTHING;

INSERT INTO categories (name, description, parent_id)
SELECT 'Racing Paddleboards', 'High-performance race SUP boards', c.id
FROM categories c WHERE c.name = 'SUP'
    ON CONFLICT (name) DO NOTHING;

INSERT INTO products (name, description, price, quantity, brand_id, category_id, board_type, status)
SELECT
    'All Star 14''0" x 24.5"',
    'The ultimate inflatable racing SUP board, combining world-leading All Star performance with airline-friendly travel package.',
    27500,
    8,
    b.id,
    c.id,
    'INFLATABLE',
    'ACTIVE'
FROM brands b, categories c
WHERE b.name = 'Starboard' AND c.name = 'Racing Paddleboards';

INSERT INTO product_attributes (product_id, attribute_name, attribute_value)
SELECT p.id, 'Length', '14''0" (426 cm)' FROM products p WHERE p.name LIKE 'All Star%';

INSERT INTO product_attributes (product_id, attribute_name, attribute_value)
SELECT p.id, 'Width', '24.5" (62 cm)' FROM products p WHERE p.name LIKE 'All Star%';

INSERT INTO product_attributes (product_id, attribute_name, attribute_value)
SELECT p.id, 'Volume', '293 L' FROM products p WHERE p.name LIKE 'All Star%';

INSERT INTO product_attributes (product_id, attribute_name, attribute_value)
SELECT p.id, 'Type', 'Race / Tour / Training' FROM products p WHERE p.name LIKE 'All Star%';