MYSQL DB:

Db name oop_project

CREATE TABLE students (
    id INT AUTO_INCREMENT PRIMARY KEY,
    last_name VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    middle_name VARCHAR(255),
    student_id VARCHAR(20) NOT NULL,
    student_email VARCHAR(255) NOT NULL,
    year_level VARCHAR(50) NOT NULL,
    tip_branch VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE items (
    item_id INT PRIMARY KEY AUTO_INCREMENT,
    user_email VARCHAR(100) NOT NULL,
    item_name VARCHAR(100) NOT NULL,
    cost DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL,
    image_path VARCHAR(255) NOT NULL,
    campus VARCHAR(50) NOT NULL,
    status VARCHAR(20) DEFAULT 'available',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_email) REFERENCES users(email)
);
