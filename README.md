MYSQL DB:

Db name oop_project

CREATE TABLE students (
    id INT AUTO_INCREMENT PRIMARY KEY,
    last_name VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    middle_name VARCHAR(255),
    student_id VARCHAR(20) NOT NULL,
    student_email VARCHAR(255) NOT NULL UNIQUE,
    year_level VARCHAR(50) NOT NULL,
    tip_branch VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE items (
    id INT(11) PRIMARY KEY AUTO_INCREMENT,
    user_email VARCHAR(255) NOT NULL,
    item_name VARCHAR(255) NOT NULL,
    cost DECIMAL(10,2) NOT NULL,
    quantity INT(11) NOT NULL,
    image_path VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    campus VARCHAR(50) NULL,
    FOREIGN KEY (user_email) REFERENCES students(student_email)
);

CREATE TABLE orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    buyer_email VARCHAR(255) NOT NULL,
    item_id INT NOT NULL,
    quantity INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (buyer_email) REFERENCES students(student_email),
    FOREIGN KEY (item_id) REFERENCES items(id)
);

CREATE TABLE order_details (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    pickup_date DATE NOT NULL,
    payment_method VARCHAR(50),
    payment_status ENUM('Unpaid', 'Paid') DEFAULT 'Unpaid',
    e_wallet_provider VARCHAR(50),
    e_wallet_number VARCHAR(50),
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE TABLE reports (
    id INT AUTO_INCREMENT PRIMARY KEY,
    reported_user_id INT NOT NULL,
    reporter_user_id INT NOT NULL,
    comment TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reported_user_id) REFERENCES students(id),
    FOREIGN KEY (reporter_user_id) REFERENCES students(id)
);
