SELECT 'CREATE DATABASE accountdb OWNER fincore' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'accountdb')\\gexec
SELECT 'CREATE DATABASE paymentdb OWNER fincore' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'paymentdb')\\gexec
SELECT 'CREATE DATABASE notificationdb OWNER fincore' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'notificationdb')\\gexec
