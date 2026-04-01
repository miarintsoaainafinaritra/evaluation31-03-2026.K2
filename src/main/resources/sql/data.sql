
INSERT INTO ingredient (id, name, price, category) VALUES
    (1, 'Laitue', 1.50, 'VEGETABLE'),
    (2, 'Tomate', 2.50, 'VEGETABLE'),
    (3, 'Poulet', 8.00, 'MEAT'),
    (4, 'Chocolat', 5.00, 'OTHER'),
    (5, 'Beurre', 3.50, 'DAIRY');

INSERT INTO ingredient_stock (ingredient_id, timestamp, stock_value) VALUES
    (1, '2024-01-01 00:00:00', 5.0),
    (2, '2024-01-01 00:00:00', 4.0),
    (3, '2024-01-01 00:00:00', 10.0),
    (4, '2024-01-01 00:00:00', 3.0),
    (5, '2024-01-01 00:00:00', 2.5);


INSERT INTO stock_movement (id, ingredient_id, quantity, unit, type, creation_datetime) VALUES
    (6, 1, 0.2, 'KG', 'OUT', '2024-01-06 12:00:00'),
    (7, 2, 0.15, 'KG', 'OUT', '2024-01-06 12:00:00'),
    (8, 3, 1.0, 'KG', 'OUT', '2024-01-06 12:00:00'),
    (9, 4, 0.3, 'KG', 'OUT', '2024-01-06 12:00:00'),
    (10, 5, 0.2, 'KG', 'OUT', '2024-01-06 12:00:00');
