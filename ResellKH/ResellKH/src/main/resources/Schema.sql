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
                         comment TEXT
);


CREATE TABLE notifications (
                               id SERIAL PRIMARY KEY,
                               user_id INTEGER REFERENCES users(user_id),
                               content TEXT,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
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

