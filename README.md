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
