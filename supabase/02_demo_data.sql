-- Datos de demostración para la primera prueba.
-- Usuario: admin
-- Contraseña temporal: Herradura2026!

INSERT INTO app_users (username, password, full_name, role, active)
VALUES (
    'admin',
    '$2a$10$06L42F7smf00U06h8aXx3.StXYHoorlhT/sCL3qittjixk/EyWCe2',
    'Administrador La Herradura',
    'ADMIN',
    TRUE
)
ON CONFLICT (username) DO NOTHING;

INSERT INTO business_settings (
    id, business_name, phone, address, opening_hours,
    delivery_enabled, minimum_delivery_amount, currency, welcome_message
)
VALUES (
    1, 'Carnicería La Herradura', '938149352', '[PENDIENTE]', '[PENDIENTE]',
    TRUE, 50.00, 'PEN', 'Calidad, frescura y sabor para tu mesa'
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO categories (name, description, active) VALUES
    ('Consumo diario', 'Cortes para guisos, sopas y comidas familiares', TRUE),
    ('Parrilla', 'Cortes premium para parrilla', TRUE),
    ('Cortes especiales', 'Cortes seleccionados y personalizados', TRUE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO products (
    category_id, name, description, price_per_unit, unit,
    stock_quantity, minimum_quantity, active, featured
)
SELECT c.id, p.name, p.description, p.price, 'KG', p.stock, 0.500, TRUE, p.featured
FROM categories c
JOIN (VALUES
    ('Consumo diario', 'Bistec', 'Ideal para freír o preparar lomo saltado', 28.00::NUMERIC, 25.000::NUMERIC, TRUE),
    ('Consumo diario', 'Carne molida', 'Para hamburguesas, tallarines y rellenos', 24.00::NUMERIC, 20.000::NUMERIC, TRUE),
    ('Consumo diario', 'Carne para guiso', 'Corte sabroso para estofados', 25.00::NUMERIC, 20.000::NUMERIC, FALSE),
    ('Consumo diario', 'Osobuco', 'Perfecto para sopas y cocción lenta', 22.00::NUMERIC, 15.000::NUMERIC, FALSE),
    ('Parrilla', 'Picaña', 'Corte con capa de grasa, jugoso para parrilla', 46.00::NUMERIC, 12.000::NUMERIC, TRUE),
    ('Parrilla', 'Ribeye', 'Alto marmoleo y gran sabor', 58.00::NUMERIC, 10.000::NUMERIC, TRUE),
    ('Parrilla', 'T-Bone', 'Lomo y filete unidos por el hueso', 55.00::NUMERIC, 8.000::NUMERIC, TRUE),
    ('Parrilla', 'Asado de tira', 'Costilla cortada transversalmente', 39.00::NUMERIC, 12.000::NUMERIC, FALSE),
    ('Parrilla', 'Entraña', 'Corte delgado de sabor intenso', 44.00::NUMERIC, 8.000::NUMERIC, FALSE),
    ('Cortes especiales', 'Lomo fino', 'Corte suave y magro', 62.00::NUMERIC, 8.000::NUMERIC, TRUE),
    ('Cortes especiales', 'Tomahawk', 'Corte premium con hueso largo', 68.00::NUMERIC, 6.000::NUMERIC, TRUE)
) AS p(category_name, name, description, price, stock, featured)
    ON c.name = p.category_name
ON CONFLICT (name) DO NOTHING;

INSERT INTO delivery_zones (name, fee, minimum_order, active) VALUES
    ('Zona cercana (0–3 km)', 5.00, 50.00, TRUE),
    ('Zona media (3–5 km)', 8.00, 70.00, TRUE)
ON CONFLICT (name) DO NOTHING;

INSERT INTO promotions (
    name, description, promotional_price, start_date, end_date, active
)
SELECT
    'Combo Parrillero',
    'Picaña, asado de tira y entraña para compartir. Precio demostrativo.',
    119.90,
    CURRENT_DATE,
    CURRENT_DATE + 30,
    TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM promotions WHERE name = 'Combo Parrillero'
);
