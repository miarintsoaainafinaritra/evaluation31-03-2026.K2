
CREATE TABLE IF NOT EXISTS ingredient (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    category VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS dish (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS dish_ingredient (
    dish_id INT NOT NULL,
    ingredient_id INT NOT NULL,
    PRIMARY KEY (dish_id, ingredient_id),
    FOREIGN KEY (dish_id) REFERENCES dish(id),
    FOREIGN KEY (ingredient_id) REFERENCES ingredient(id)
);

CREATE TABLE IF NOT EXISTS ingredient_stock (
    ingredient_id INT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    stock_value DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (ingredient_id, timestamp),
    FOREIGN KEY (ingredient_id) REFERENCES ingredient(id)
);

CREATE TABLE IF NOT EXISTS stock_movement (
    id SERIAL PRIMARY KEY,
    ingredient_id INT NOT NULL,
    quantity DOUBLE PRECISION NOT NULL,
    unit VARCHAR(10) NOT NULL,
    type VARCHAR(10) NOT NULL,
    creation_datetime TIMESTAMP NOT NULL,
    FOREIGN KEY (ingredient_id) REFERENCES ingredient(id)
);

