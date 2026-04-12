CREATE TABLE IF NOT EXISTS orders (
    order_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(255) NOT NULL,
    shopping_cart_id UUID,
    payment_id UUID,
    delivery_id UUID,
    state VARCHAR(50) NOT NULL CHECK (state IN (
        'NEW', 'ON_PAYMENT', 'ON_DELIVERY', 'DONE', 'DELIVERED',
        'ASSEMBLED', 'PAID', 'COMPLETED', 'DELIVERY_FAILED',
        'ASSEMBLY_FAILED', 'PAYMENT_FAILED', 'PRODUCT_RETURNED', 'CANCELED'
    )),
    delivery_weight DOUBLE PRECISION NOT NULL DEFAULT 0,
    delivery_volume DOUBLE PRECISION NOT NULL DEFAULT 0,
    fragile BOOLEAN NOT NULL DEFAULT FALSE,
    total_price DOUBLE PRECISION,
    delivery_price DOUBLE PRECISION,
    product_price DOUBLE PRECISION
);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    product_id UUID NOT NULL,
    quantity BIGINT NOT NULL CHECK (quantity > 0),
    price DOUBLE PRECISION NOT NULL CHECK (price >= 0),
    UNIQUE(order_id, product_id)
);