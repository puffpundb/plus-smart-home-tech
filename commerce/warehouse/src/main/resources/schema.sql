CREATE TABLE IF NOT EXISTS warehouse (
    product_id UUID PRIMARY KEY,
    weight DOUBLE PRECISION NOT NULL CHECK (weight >= 1),
    width DOUBLE PRECISION NOT NULL CHECK (width >= 1),
    height DOUBLE PRECISION NOT NULL CHECK (height >= 1),
    depth DOUBLE PRECISION NOT NULL CHECK (depth >= 1),
    fragile BOOLEAN NOT NULL DEFAULT FALSE,
    quantity BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS order_bookings (
    booking_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL UNIQUE,
    delivery_id UUID,
    total_weight DOUBLE PRECISION,
    total_volume DOUBLE PRECISION,
    has_fragile BOOLEAN
);

CREATE TABLE IF NOT EXISTS order_booking_items (
    item_id BIGSERIAL PRIMARY KEY,
    booking_id UUID NOT NULL REFERENCES order_bookings(booking_id) ON DELETE CASCADE,
    product_id UUID NOT NULL,
    quantity BIGINT NOT NULL CHECK (quantity > 0),

    UNIQUE (booking_id, product_id)
);