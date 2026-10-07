# Student Management System

A web-based Student Management System developed using Java, Spring Boot, MySQL, HTML, CSS, and JavaScript.

The application provides separate workflows for Admin and Student users, including student registration, approval management, student record management, and profile management.

## Features

### Student
- Student registration request
- Admin approval-based account activation
- Student dashboard
- View and update personal information
- Change username and password
- Secure login

### Admin
- Admin login
- View student records
- Search student information
- Review pending student registration requests
- Approve or reject student registration requests
- Add new admin accounts
- Update student information
- Delete student records
- Update admin profile

## Technologies Used

- Java
- Spring Boot
- MySQL
- JDBC
- HTML
- CSS
- JavaScript
- Maven
- Git & GitHub

## Project Structure

```text
src/
├── main/
│   ├── java/
│   │   └── com/example/demo/
│   │       ├── controller/
│   │       ├── model/
│   │       ├── repository/
│   │       ├── service/
│   │       ├── security/
│   │       └── DemoApplication.java
│   │
│   └── resources/
│       ├── static/
│       └── templates/
│
├── pom.xml
└── README.md