CREATE TABLE products (
                          product_id SERIAL PRIMARY KEY,
                          product_name VARCHAR(255),
                          user_id INTEGER REFERENCES users(user_id),
                          main_category_id INTEGER REFERENCES main_category(main_category_id),
                          product_price DOUBLE PRECISION,
                          discount_percent DOUBLE PRECISION,
                          product_status VARCHAR(50),
                          description TEXT,
                          location TEXT,
                          latitude DOUBLE PRECISION,
                          longitude DOUBLE PRECISION,
                          condition VARCHAR(50),
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP

);
SELECT * FROM users;
ALTER TABLE products ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;



CREATE TABLE users (
                       user_id SERIAL PRIMARY KEY,
                       user_name VARCHAR(50),
                       first_name VARCHAR(50),
                       last_name VARCHAR(50),
                       email VARCHAR(100) UNIQUE,
                       password VARCHAR(255),
                       role VARCHAR(20),
                       enabled BOOLEAN DEFAULT FALSE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);



ALTER TABLE users ADD COLUMN profile_image VARCHAR(255);
CREATE TABLE user_profile (
                              profile_id SERIAL PRIMARY KEY,
                              user_id BIGINT REFERENCES users(user_id) ON DELETE CASCADE UNIQUE,
                              birthday DATE,
                              gender VARCHAR(10),
                              phone_number VARCHAR(20),
                              profile_image TEXT,
                              cover_image TEXT,
                              address TEXT
);
ALTER TABLE user_profile
    ADD COLUMN is_seller BOOLEAN DEFAULT FALSE;
UPDATE user_profile SET first_name = 'loun', last_name = 'siven' WHERE user_id = 2;
--Add New Column
ALTER TABLE user_profile ADD COLUMN telegram_url VARCHAR(255),
                        ADD COLUMN slogan TEXT,
                        ADD COLUMN user_name VARCHAR(50),
                        ADD COLUMN first_name VARCHAR(50),
                        ADD COLUMN last_name VARCHAR(50);
ALTER TABLE user_profile ADD COLUMN address_map TEXT;
UPDATE user_profile SET slogan = 'SUSU, Youre Very Strong' WHERE profile_id = 2;
DELETE FROM users WHERE user_id = 7;

INSERT INTO user_profile(user_id, first_name, last_name) VALUES (6, 'loun', 'siven');
UPDATE user_profile SET profile_image = 'https://gateway.pinata.cloud/ipfs/QmQ2pi5ptqpvJixDEsLRQxDQafcJGLv6hhjKxZUb2cp9iQ' WHERE profile_id = 3;;

CREATE TABLE product_images (
                                id SERIAL PRIMARY KEY,
                                product_id INTEGER REFERENCES products(product_id),
                                url TEXT
);
CREATE TABLE product_embeddings (
                                    id SERIAL PRIMARY KEY,
                                    product_id BIGINT REFERENCES products(product_id) ON DELETE CASCADE UNIQUE,
                                    vector JSONB NOT NULL
);



CREATE TABLE product_history (
                                 id SERIAL PRIMARY KEY,
                                 product_id INTEGER NOT NULL, -- keep required
                                 message TEXT,
                                 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE main_category (
                               main_category_id SERIAL PRIMARY KEY,
                               name VARCHAR(100)
);
CREATE TABLE favourites (
                            favourite_id SERIAL PRIMARY KEY,
                            user_id INT NOT NULL,
                            product_id INT NOT NULL,
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            CONSTRAINT unique_fav UNIQUE (user_id, product_id)
);



CREATE TABLE ratings (
                         rating_id SERIAL PRIMARY KEY,
                         rated_user_id INTEGER REFERENCES users(user_id),
                         rating_user_id INTEGER REFERENCES users(user_id),
                         score INTEGER,
                         comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP

);
ALTER TABLE ratings ALTER COLUMN score TYPE DECIMAL(2,1);


CREATE TABLE notifications (
                               id SERIAL PRIMARY KEY,
                               user_id INTEGER REFERENCES users(user_id),
                               content TEXT,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE product_drafts (
                                draft_id SERIAL PRIMARY KEY,
                                product_name VARCHAR(255),
                                user_id INTEGER REFERENCES users(user_id),
                                main_category_id INTEGER REFERENCES main_category(main_category_id),
                                product_price DOUBLE PRECISION,
                                discount_percent DOUBLE PRECISION,
                                product_status VARCHAR(50) DEFAULT 'draft', -- to mark draft status
                                description TEXT,
                                location TEXT,
                                latitude DOUBLE PRECISION,
                                longitude DOUBLE PRECISION,
                                condition VARCHAR(50),
                                telegram_url VARCHAR(255),
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE product_draft_images (
                                      id SERIAL PRIMARY KEY,
                                      draft_id INTEGER REFERENCES product_drafts(draft_id) ON DELETE CASCADE,
                                      url TEXT NOT NULL
);

select * from product_drafts;;

CREATE TABLE seller (
                        seller_id SERIAL PRIMARY KEY,
                        user_id INTEGER REFERENCES users(user_id),
                        business_name VARCHAR(255),
                        business_type VARCHAR(255),
                        Business_address TEXT,
                        Business_description TEXT,
                        expected_revenue DOUBLE PRECISION,

                        bank_name VARCHAR(255),
                        bank_account_name VARCHAR(255),
                        bank_account_number VARCHAR(255),
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


INSERT INTO seller (
    user_id,
    business_name,
    business_type,
    Business_address,
    Business_description,
    expected_revenue,
    bank_name,
    bank_account_name,
    bank_account_number
) VALUES (
             6, -- user_id
             'Sample Business Co.',
             'company',
             '123 Main Street, Phnom Penh, Cambodia',
             'Selling electronics and home goods online.',
             5500.00,
             'ABA Bank',
             'John Doe',
             '000-123456789'
         );

ALTER TABLE notifications
    ADD COLUMN title VARCHAR(255);

ALTER TABLE notifications
    ADD COLUMN type VARCHAR(255) DEFAULT 'info';

ALTER TABLE notifications
    ADD COLUMN is_read BOOLEAN DEFAULT FALSE;

ALTER TABLE notifications
    ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE notifications
    ADD CONSTRAINT fk_user
        FOREIGN KEY (user_id)
            REFERENCES users(user_id)
            ON DELETE CASCADE;

INSERT INTO notifications (
    user_id,
    content,
    title,
    type_id,
    is_read,
    created_at,
    updated_at
) VALUES (
             6,
             'Welcome to ResellKH! We’re excited to have you join our community. As a new member, you can explore great deals, post your products, and connect with trusted buyers and sellers. Stay updated with the latest promotions, features, and security tips. Thank you for choosing ResellKH — let’s grow together!

',
             'Welcome Message',
             1,
             FALSE,
             NOW(),
             NOW()
         );

UPDATE notifications SET is_read = TRUE WHERE id = 5;

delete from product_embeddings where product_id = 83;

ALTER TABLE notifications
    DROP COLUMN type;
ALTER TABLE notifications
    ADD COLUMN type_id INTEGER;
ALTER TABLE notifications
    ADD CONSTRAINT fk_notification_type
        FOREIGN KEY (type_id)
            REFERENCES notification_type(type_id)
            ON DELETE SET NULL; -- or CASCADE, depending on your logic


CREATE TABLE notification_type (
        type_id SERIAL PRIMARY KEY,
        type_name VARCHAR(100) NOT NULL,
        icon_url TEXT
);

ALTER TABLE products ADD COLUMN telegram_link VARCHAR(255);
INSERT INTO notification_type (type_name, icon_url)
VALUES ('system', 'https://gateway.pinata.cloud/ipfs/QmUWSe4FtEp6UFHg2BuN1HKvetKM7Vj8o7AAg6iE8saALG'),
       ('supporter', 'https://gateway.pinata.cloud/ipfs/QmbiJkxp8UJDF4e53ECzKKH8tCokVsAjTdGRqPZ3Zwtxdg'),
       ('promotion', 'https://gateway.pinata.cloud/ipfs/QmQc2zePL3iDSEMqUjsUTt9RrtiK1QRCDe4TNnybgn4rcf'),
       ('alert', 'https://gateway.pinata.cloud/ipfs/QmdnXCmFZFySD2NMLu2eUSHBi2PR6HonG8cwsTgVna5F6G'),
       ('feedback', 'https://gateway.pinata.cloud/ipfs/QmeA57noWeBe1VzGnMjPpmPNeZVvUAFnmhgikenFgAwH6Q');
UPDATE notifications SET type_id = 4 WHERE id = 4;


CREATE TABLE otp_number (
                            id SERIAL PRIMARY KEY,
                            email VARCHAR(255) NOT NULL,
                            otp VARCHAR(10) NOT NULL,
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            verified BOOLEAN DEFAULT FALSE
);


CREATE TABLE contact_info (
                              id SERIAL PRIMARY KEY,
                              user_id INTEGER REFERENCES users(user_id),
                              telegram_url VARCHAR(255)
);

INSERT INTO main_category (name) VALUES
                                     ('Accessories'),
                                     ('Beauty'),
                                     ('Equipment Bag & Shoes'),
                                     ('Book'),
                                     ('Fashion'),
                                     ('Home'),
                                     ('Sports & Kids'),
                                     ('Electronic'),
                                     ('Vehicle'),
                                     ('Other');
CREATE TABLE cart (
                      cart_id SERIAL PRIMARY KEY,
                      user_id INTEGER REFERENCES users(user_id) ON DELETE CASCADE,
                      product_id INTEGER REFERENCES products(product_id) ON DELETE CASCADE,
                      quantity INTEGER DEFAULT 1,
                      added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      UNIQUE (user_id, product_id)
);
CREATE TABLE orders (
                        order_id SERIAL PRIMARY KEY,
                        user_id INTEGER REFERENCES users(user_id) ON DELETE CASCADE,
                        total_amount DECIMAL(10, 2),
                        status VARCHAR(50) DEFAULT 'pending', -- pending, paid, failed, cancelled
                        payment_method VARCHAR(50), -- e.g., ABA, ACLEDA, Wing
                        payment_reference VARCHAR(255), -- Transaction ID from bank
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE order_items (
                             order_item_id SERIAL PRIMARY KEY,
                             order_id INTEGER REFERENCES orders(order_id) ON DELETE CASCADE,
                             product_id INTEGER REFERENCES products(product_id),
                             quantity INTEGER,
                             price_at_order DECIMAL(10, 2)
);

CREATE TABLE payment_transactions (
                                      transaction_id SERIAL PRIMARY KEY,
                                      order_id INTEGER REFERENCES orders(order_id) ON DELETE CASCADE,
                                      bank_name VARCHAR(100),
                                      transaction_code VARCHAR(255),
                                      status VARCHAR(50), -- pending, success, failed
                                      response TEXT,
                                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

truncate table seller cascade ;







TRUNCATE TABLE users restart  identity  cascade;
delete from user_profile where user_id = 23;
DELETE FROM product_images WHERE product_id = 87;
DELETE FROM products WHERE product_id = 87;
UPDATE products SET user_id = 6 WHERE product_id >16;
ALTER TABLE products RENAME COLUMN telegram_link TO telegram_url;

UPDATE products SET latitude = 11.5564, longitude = 104.9282 WHERE product_id >=20;
select * from products where product_id >80;
UPDATE user_profile SET profile_image = 'https://gateway.pinata.cloud/ipfs/QmNb6F1KSKypzBLa9pawQhGcjUmUubpeHoyXBeMyEnsUHK' WHERE profile_id = 16;
UPDATE notification_type SET icon_url = 'https://gateway.pinata.cloud/ipfs/QmdMXVZ9KCiNGMwFHxkPMfpUfeGL8QQpMoENKeR5NKJ51F' WHERE type_id = 1;

CREATE TABLE order (
                        order_id SERIAL PRIMARY KEY,
                        buyer_id INTEGER REFERENCES users(user_id),
                        total_amount DOUBLE PRECISION NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
TRUNCATE TABLE product_draft_images CASCADE;

ALTER TABLE product_draft_images DROP CONSTRAINT product_draft_images_draft_id_fkey;

-- Fix column type
ALTER TABLE product_draft_images
    ALTER COLUMN draft_id TYPE BIGINT USING draft_id::BIGINT;

-- Re-add the foreign key
ALTER TABLE product_draft_images
    ADD CONSTRAINT product_draft_images_draft_id_fkey
        FOREIGN KEY (draft_id) REFERENCES product_drafts(draft_id) ON DELETE CASCADE;


CREATE TABLE order_items (
                             order_item_id SERIAL PRIMARY KEY,
                             order_id INTEGER REFERENCES orders(order_id) ON DELETE CASCADE,
                             product_id INTEGER REFERENCES products(product_id),
                             seller_id INTEGER REFERENCES seller(seller_id),
                             price DOUBLE PRECISION NOT NULL
);

DROP TABLE seller;
SELECT * FROM seller;
SELECT * FROM orders;
SELECT * FROM order_items;
DROP TABLE orders CASCADE;
DROP TABLE order_items;

CREATE TABLE orders (
                        order_id SERIAL PRIMARY KEY,
                        buyer_id INTEGER REFERENCES users(user_id),
                        full_name VARCHAR(255),
                        phone_number VARCHAR(10),
                        address TEXT,
                        sub_total DOUBLE PRECISION NOT NULL,
                        delivery DOUBLE PRECISION,
                        total_amount DOUBLE PRECISION NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE orders ADD COLUMN  status VARCHAR(20) DEFAULT 'PENDING';
--PENDING, PAID, SHIPPED, DELIVERED

CREATE TABLE order_items (
                             order_item_id SERIAL PRIMARY KEY,
                             order_id INTEGER REFERENCES orders(order_id) ON DELETE CASCADE,
                             product_id INTEGER REFERENCES products(product_id),
                             price DOUBLE PRECISION NOT NULL,
                             seller_id INTEGER REFERENCES users(user_id)
);

CREATE TABLE payments (
                          payment_id SERIAL PRIMARY KEY,
                          order_id INTEGER NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
                          buyer_id INTEGER REFERENCES users(user_id),
                          amount DOUBLE PRECISION NOT NULL,
                          method VARCHAR(50) NOT NULL,                    -- e.g. CARD, BANK_TRANSFER, QR_CODE
                          status VARCHAR(20) DEFAULT 'SUCCESS',          -- e.g. SUCCESS, FAILED, PENDING
                          transaction_reference VARCHAR(255),            -- optional, bank or gateway transaction id
                          paid_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE seller_settlements (
                                    settlement_id SERIAL PRIMARY KEY,
                                    seller_id INTEGER NOT NULL REFERENCES seller(seller_id) ON DELETE CASCADE,
                                    order_item_id INTEGER REFERENCES order_items(order_item_id),
                                    amount DOUBLE PRECISION NOT NULL,
                                    status VARCHAR(20) DEFAULT 'PENDING',          -- e.g. PENDING, COMPLETED, FAILED
                                    scheduled_at TIMESTAMP NOT NULL,              -- planned payout date
                                    settled_at TIMESTAMP                           -- actual payout timestamp
);

SELECT * FROM seller;
ALTER TABLE payments ADD COLUMN bank_name VARCHAR(255);

SELECT COUNT(*)
FROM products
WHERE user_id = 6; -- Replace 123 with the actual seller_id you want to count for












