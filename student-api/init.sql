CREATE TABLE IF NOT EXISTS students (
    id SERIAL PRIMARY KEY,
    student_code VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    major VARCHAR(100) NOT NULL
);
INSERT INTO students (student_code, name, major) VALUES
('65001', 'Somchai Jaidee', 'Computer Science'),
('65002', 'Somsri Dee', 'Information Technology'),
('65003', 'Anan Kong', 'Data Science');
