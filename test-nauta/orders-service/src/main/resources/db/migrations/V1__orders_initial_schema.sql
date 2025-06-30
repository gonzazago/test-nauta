-- Table: purchase_orders
CREATE TABLE IF NOT EXISTS purchase_orders (
    purchase_order_id VARCHAR(50) PRIMARY KEY,
    client_id VARCHAR(50) NOT NULL, -- Logical FK to Customer Service
    booking_id VARCHAR(50)        -- Logical FK to Booking Service, NULLABLE
);

-- Table: invoices
CREATE TABLE IF NOT EXISTS invoices (
    invoice_id VARCHAR(50) PRIMARY KEY,
    client_id VARCHAR(50) NOT NULL, -- Logical FK to Customer Service
    purchase_order_id VARCHAR(50),  -- FK to purchase_orders (Physical FK)
    FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(purchase_order_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS order_containers (
    purchase_order_id VARCHAR(50) NOT NULL, -- FK to purchase_orders
    container_id VARCHAR(50) NOT NULL,     -- Logical FK to Container Service
    client_id VARCHAR(50) NOT NULL,         -- Ensures client data isolation
    PRIMARY KEY (purchase_order_id, container_id), -- Composite Primary Key
    FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(purchase_order_id) ON DELETE CASCADE
);