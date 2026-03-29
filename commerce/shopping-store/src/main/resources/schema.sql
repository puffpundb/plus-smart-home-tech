CREATE TABLE IF NOT EXISTS product (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    image_src VARCHAR(500),
    quantity_state VARCHAR(20) NOT NULL CHECK (quantity_state IN ('ENDED', 'FEW', 'ENOUGH', 'MANY')),
    product_state VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (product_state IN ('ACTIVE', 'DEACTIVATE')),
    product_category VARCHAR(20) NOT NULL CHECK (product_category IN ('LIGHTING', 'CONTROL', 'SENSORS')),
    price NUMERIC(10, 2) NOT NULL CHECK (price >= 1)
);