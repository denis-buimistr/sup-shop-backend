-- Старая цена для товаров со скидкой: «Sale» — это не категория, а товары, где compare_at_price > price
ALTER TABLE products
    ADD COLUMN compare_at_price NUMERIC(10, 2),
    ADD CONSTRAINT chk_products_compare_at_price CHECK (compare_at_price IS NULL OR compare_at_price > price);

-- Верхний уровень. SUP уже есть (V8), обновляю ему описание
UPDATE categories SET description = 'Stand up paddleboards, paddles and gear' WHERE slug = 'sup';

INSERT INTO categories (name, slug, description, parent_id) VALUES
    ('Foil & Wing', 'foil-wing', 'Wings, foil boards and foils', NULL),
    ('Kitesurf', 'kitesurf', 'Kites, twintips and control bars', NULL),
    ('Surf', 'surf', 'Surfboards and surf accessories', NULL),
    ('Kayak', 'kayak', 'Inflatable kayaks and paddles', NULL),
    ('Wetsuits', 'wetsuits', 'Wetsuits and neoprene for every season', NULL),
    ('Clothing', 'clothing', 'Clothing and changing robes', NULL),
    ('Accessories', 'accessories', 'Pumps, bags, buoyancy aids and transport', NULL);

-- Racing Paddleboards из V8 становится общей веткой для досок под дистанцию
UPDATE categories
SET name = 'Touring & Race', slug = 'touring-race', description = 'Long, fast boards for distance and racing'
WHERE slug = 'racing-paddleboards';

-- Подкатегории: родителя ищу по slug, чтобы не зависеть от id
INSERT INTO categories (name, slug, description, parent_id)
SELECT c.name, c.slug, c.description, p.id
FROM (VALUES
    ('sup', 'Inflatable Boards', 'inflatable-boards', 'Inflatable SUP boards and packages'),
    ('sup', 'Composite Boards', 'composite-boards', 'Hard boards: epoxy and carbon'),
    ('sup', 'Paddles', 'paddles', 'Fixed, two and three piece paddles'),
    ('sup', 'SUP Accessories', 'sup-accessories', 'Fins, leashes, pumps and deck bags'),
    ('foil-wing', 'Wings', 'wings', 'Inflatable wings for wing foiling'),
    ('foil-wing', 'Foil Boards', 'foil-boards', 'Wing, SUP and downwind foil boards'),
    ('foil-wing', 'Foils', 'foils', 'Complete foils, front wings and masts'),
    ('kitesurf', 'Kites', 'kites', 'Freeride, freestyle and trainer kites'),
    ('kitesurf', 'Twintips', 'twintips', 'Twintip kiteboards'),
    ('kitesurf', 'Bars & Harnesses', 'bars-harnesses', 'Control bars, harnesses and leashes'),
    ('surf', 'Surfboards', 'surfboards', 'Softboards, funboards and shortboards'),
    ('surf', 'Surf Accessories', 'surf-accessories', 'Leashes, wax, pads and board bags'),
    ('kayak', 'Inflatable Kayaks', 'inflatable-kayaks', 'One and two seat inflatable kayaks'),
    ('kayak', 'Kayak Paddles', 'kayak-paddles', 'Two and four piece kayak paddles'),
    ('wetsuits', 'Men''s Wetsuits', 'mens-wetsuits', 'Full suits, shorties and long johns'),
    ('wetsuits', 'Women''s Wetsuits', 'womens-wetsuits', 'Full suits, shorties and long janes'),
    ('wetsuits', 'Kids'' Wetsuits', 'kids-wetsuits', 'Wetsuits for young paddlers'),
    ('wetsuits', 'Neoprene Accessories', 'neoprene-accessories', 'Boots, gloves, hoods and socks'),
    ('clothing', 'Men''s Clothing', 'mens-clothing', 'T-shirts, hoodies and watershirts'),
    ('clothing', 'Women''s Clothing', 'womens-clothing', 'T-shirts, hoodies and watershirts'),
    ('clothing', 'Changing Robes', 'changing-robes', 'Waterproof and towel changing robes'),
    ('accessories', 'Pumps', 'pumps', 'Hand and electric pumps'),
    ('accessories', 'Waterproof Bags', 'waterproof-bags', 'Dry bags and waterproof backpacks'),
    ('accessories', 'Buoyancy Aids', 'buoyancy-aids', 'Buoyancy aids and impact vests'),
    ('accessories', 'Car & Transport', 'car-transport', 'Roof straps, pads and board bags')
) AS c(parent_slug, name, slug, description)
JOIN categories p ON p.slug = c.parent_slug;

-- Товары, которые висели прямо на SUP или без категории, раскладываю по типу доски
UPDATE products
SET category_id = (SELECT id FROM categories WHERE slug =
        CASE WHEN board_type = 'HARDBOARD' THEN 'composite-boards' ELSE 'inflatable-boards' END)
WHERE category_id IS NULL
   OR category_id = (SELECT id FROM categories WHERE slug = 'sup');
