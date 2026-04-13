CREATE TABLE IF NOT EXISTS deliveries (
    delivery_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL CHECK (status IN ('CREATED', 'IN_PROGRESS', 'DELIVERED', 'FAILED', 'CANCELLED')),
    from_country VARCHAR(100),
    from_city VARCHAR(100),
    from_street VARCHAR(200),
    from_house VARCHAR(20),
    from_flat VARCHAR(20),
    to_country VARCHAR(100),
    to_city VARCHAR(100),
    to_street VARCHAR(200),
    to_house VARCHAR(20),
    to_flat VARCHAR(20),
    weight DOUBLE PRECISION,
    volume DOUBLE PRECISION,
    fragile BOOLEAN
);