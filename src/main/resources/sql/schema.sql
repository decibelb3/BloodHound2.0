-- BloodHound 2.0 — run as MySQL user with privileges on database bloodhound2
CREATE DATABASE IF NOT EXISTS bloodhound2
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE bloodhound2;

CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    UNIQUE KEY uk_users_username (username),
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS measurements (
    measurement_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    systolic INT NULL,
    diastolic INT NULL,
    total_cholesterol DECIMAL(10, 2) NULL,
    hdl DECIMAL(10, 2) NULL,
    ldl DECIMAL(10, 2) NULL,
    weight DECIMAL(10, 2) NULL,
    measurement_datetime DATETIME NOT NULL,
    CONSTRAINT fk_measurements_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON DELETE CASCADE,
    KEY idx_measurements_user_datetime (user_id, measurement_datetime)
) ENGINE=InnoDB;

-- Optional: create application user (run as MySQL root or admin)
-- CREATE USER IF NOT EXISTS 'bloodhound_user'@'localhost' IDENTIFIED BY 'change_me';
-- GRANT ALL PRIVILEGES ON bloodhound2.* TO 'bloodhound_user'@'localhost';
-- FLUSH PRIVILEGES;
