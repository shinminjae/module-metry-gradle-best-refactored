CREATE TABLE IF NOT EXISTS iot_messages (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    device_id TEXT NOT NULL,
    topic TEXT NOT NULL,
    payload TEXT NOT NULL,
    data_type TEXT,
    value REAL,
    received_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    raw_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
); 