-- Insert users only if they don't exist
INSERT INTO users (name, email, password, role, created_at)
SELECT 'Admin', 'admin@shop.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@shop.com');

INSERT INTO users (name, email, password, role, created_at)
SELECT 'John Doe', 'john@shop.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'CUSTOMER', NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'john@shop.com');

INSERT INTO users (name, email, password, role, created_at)
SELECT 'Jane Smith', 'jane@shop.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'CUSTOMER', NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'jane@shop.com');

-- Insert categories only if they don't exist
INSERT INTO categories (name, description, created_at)
SELECT 'Electronics', 'Gadgets, devices, and electronic equipment', NOW()
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Electronics');

INSERT INTO categories (name, description, created_at)
SELECT 'Clothing', 'Men and women fashion and apparel', NOW()
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Clothing');

INSERT INTO categories (name, description, created_at)
SELECT 'Books', 'Fiction, non-fiction, textbooks and more', NOW()
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Books');

INSERT INTO categories (name, description, created_at)
SELECT 'Home & Kitchen', 'Furniture, appliances, and kitchen essentials', NOW()
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Home & Kitchen');

INSERT INTO categories (name, description, created_at)
SELECT 'Sports', 'Sportswear, equipment, and outdoor gear', NOW()
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Sports');

-- Insert products only if they don't exist
INSERT INTO products (name, description, price, stock_quantity, image_url, created_at, updated_at, category_id)
SELECT 'MacBook Pro 14"', 'Apple MacBook Pro with M3 chip, 16GB RAM, 512GB SSD', 1999.99, 50,
       'https://images.unsplash.com/photo-1517336714731-489689fd1ca8', NOW(), NOW(),
       (SELECT id FROM categories WHERE name = 'Electronics')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'MacBook Pro 14"');

INSERT INTO products (name, description, price, stock_quantity, image_url, created_at, updated_at, category_id)
SELECT 'Sony WH-1000XM5 Headphones', 'Industry-leading noise canceling wireless headphones', 349.99, 120,
       'https://images.unsplash.com/photo-1505740420928-5e560c06d30e', NOW(), NOW(),
       (SELECT id FROM categories WHERE name = 'Electronics')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Sony WH-1000XM5 Headphones');

INSERT INTO products (name, description, price, stock_quantity, image_url, created_at, updated_at, category_id)
SELECT 'Samsung 4K QLED TV 55"', 'Smart TV with Quantum Dot technology and HDR support', 799.99, 30,
       'https://images.unsplash.com/photo-1593359677879-a4bb92f829e1', NOW(), NOW(),
       (SELECT id FROM categories WHERE name = 'Electronics')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Samsung 4K QLED TV 55"');

INSERT INTO products (name, description, price, stock_quantity, image_url, created_at, updated_at, category_id)
SELECT 'iPhone 15 Pro', 'Apple iPhone 15 Pro with A17 Pro chip and titanium design', 1199.99, 75,
       'https://images.unsplash.com/photo-1592750475338-74b7b21085ab', NOW(), NOW(),
       (SELECT id FROM categories WHERE name = 'Electronics')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'iPhone 15 Pro');

INSERT INTO products (name, description, price, stock_quantity, image_url, created_at, updated_at, category_id)
SELECT 'Men''s Classic Oxford Shirt', 'Premium cotton slim-fit oxford button-down shirt', 59.99, 200,
       'https://images.unsplash.com/photo-1602810316693-3667c854239a', NOW(), NOW(),
       (SELECT id FROM categories WHERE name = 'Clothing')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Men''s Classic Oxford Shirt');

INSERT INTO products (name, description, price, stock_quantity, image_url, created_at, updated_at, category_id)
SELECT 'Women''s Running Leggings', 'High-waist compression leggings with moisture-wicking fabric', 49.99, 300,
       'https://images.unsplash.com/photo-1506629082955-511b1aa562c8', NOW(), NOW(),
       (SELECT id FROM categories WHERE name = 'Clothing')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Women''s Running Leggings');

INSERT INTO products (name, description, price, stock_quantity, image_url, created_at, updated_at, category_id)
SELECT 'Denim Jacket', 'Classic stonewashed denim jacket with button closure', 89.99, 150,
       'https://images.unsplash.com/photo-1551537482-f2075a1d41f2', NOW(), NOW(),
       (SELECT id FROM categories WHERE name = 'Clothing')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Denim Jacket');

INSERT INTO products (name, description, price, stock_quantity, image_url, created_at, updated_at, category_id)
SELECT 'Clean Code by Robert Martin', 'A handbook of agile software craftsmanship for developers', 34.99, 500,
       'https://images.unsplash.com/photo-1532012197267-da84d127e765', NOW(), NOW(),
       (SELECT id FROM categories WHERE name = 'Books')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Clean Code by Robert Martin');

INSERT INTO products (name, description, price, stock_quantity, image_url, created_at, updated_at, category_id)
SELECT 'The Pragmatic Programmer', '20th Anniversary Edition - your journey to mastery', 42.99, 400,
       'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c', NOW(), NOW(),
       (SELECT id FROM categories WHERE name = 'Books')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'The Pragmatic Programmer');

INSERT INTO products (name, description, price, stock_quantity, image_url, created_at, updated_at, category_id)
SELECT 'Atomic Habits', 'An easy & proven way to build good habits & break bad ones', 18.99, 600,
       'https://images.unsplash.com/photo-1589829085413-56de8ae18c73', NOW(), NOW(),
       (SELECT id FROM categories WHERE name = 'Books')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Atomic Habits');

INSERT INTO products (name, description, price, stock_quantity, image_url, created_at, updated_at, category_id)
SELECT 'Instant Pot Duo 7-in-1', 'Electric pressure cooker, slow cooker, rice cooker and more', 89.99, 80,
       'https://images.unsplash.com/photo-1585515320310-259814833e62', NOW(), NOW(),
       (SELECT id FROM categories WHERE name = 'Home & Kitchen')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Instant Pot Duo 7-in-1');

INSERT INTO products (name, description, price, stock_quantity, image_url, created_at, updated_at, category_id)
SELECT 'Ergonomic Office Chair', 'Mesh back lumbar support chair with adjustable armrests', 299.99, 45,
       'https://images.unsplash.com/photo-1541558869434-2840d308329a', NOW(), NOW(),
       (SELECT id FROM categories WHERE name = 'Home & Kitchen')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Ergonomic Office Chair');

INSERT INTO products (name, description, price, stock_quantity, image_url, created_at, updated_at, category_id)
SELECT 'Yoga Mat Premium', 'Non-slip 6mm thick exercise yoga mat with carrying strap', 39.99, 250,
       'https://images.unsplash.com/photo-1601925228732-f94c6ef3dc1b', NOW(), NOW(),
       (SELECT id FROM categories WHERE name = 'Sports')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Yoga Mat Premium');

INSERT INTO products (name, description, price, stock_quantity, image_url, created_at, updated_at, category_id)
SELECT 'Running Shoes Pro', 'Lightweight breathable running shoes with energy return cushioning', 129.99, 180,
       'https://images.unsplash.com/photo-1542291026-7eec264c27ff', NOW(), NOW(),
       (SELECT id FROM categories WHERE name = 'Sports')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Running Shoes Pro');

INSERT INTO products (name, description, price, stock_quantity, image_url, created_at, updated_at, category_id)
SELECT 'Adjustable Dumbbell Set', '5-50 lbs adjustable dumbbells with quick-change mechanism', 349.99, 60,
       'https://images.unsplash.com/photo-1534438327276-14e5300c3a48', NOW(), NOW(),
       (SELECT id FROM categories WHERE name = 'Sports')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Adjustable Dumbbell Set');
