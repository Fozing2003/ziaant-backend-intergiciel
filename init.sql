SELECT 'CREATE DATABASE restaurant_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'restaurant_db')\gexec
SELECT 'CREATE DATABASE reservation_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'reservation_db')\gexec
SELECT 'CREATE DATABASE notification_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'notification_db')\gexec