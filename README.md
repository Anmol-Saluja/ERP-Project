# University ERP System

A robust, robust desktop-based Enterprise Resource Planning (ERP) application for universities, built using Java Swing and JDBC. This system enables administrators, instructors, and students to manage and interact with university data, including course enrollments, grading, notifications, and user profiles.

## Key Features

- **Role-Based Access Control**: Separate interfaces and permissions for Students, Instructors, and Administrators.
- **Course & Section Management**: Create, update, and manage university courses and their respective sections.
- **Student Enrollment**: Students can view available sections, enroll, and drop courses.
- **Grading System**: Instructors can manage and submit grades for enrolled students.
- **Secure Authentication**: Passwords securely hashed using jBcrypt.
- **Database Connection Pooling**: Optimized database access using HikariCP.
- **Reporting & Backups**: Support for creating file-based database backups.

## Technology Stack

- **Language**: Java 17
- **UI Framework**: Java Swing
- **Build Tool**: Maven
- **Database**: MariaDB / MySQL
- **Connection Pooling**: HikariCP
- **Security**: jBcrypt (Password Hashing)
- **Logging**: SLF4J

## Prerequisites

Before running the application, ensure you have the following installed:

- **Java Development Kit (JDK) 17** or higher
- **Apache Maven** (for building dependencies)
- **MariaDB** or **MySQL** server running locally or remotely

## Setup & Installation

### 1. Database Setup

The application uses two separate databases: `auth_db` (for authentication/users) and `erp_db` (for core university data like courses and enrollments).

1. Log in to your MariaDB/MySQL server.
2. Create the required databases (if your seed script doesn't do it automatically):
   ```sql
   CREATE DATABASE auth_db;
   CREATE DATABASE erp_db;
   ```
3. Import the seeded database structure and initial data located in the `Database` directory:
   - Import the `SeededDat.sql` file into your local server.

### 2. Configure Database Connection

Open the `project/src/main/resources/app.properties` file and update it with your local database credentials (username and password):

```properties
auth.jdbcUrl=jdbc:mariadb://localhost:3306/auth_db?allowPublicKeyRetrieval=true&useSSL=false
auth.username=root
auth.password=your_database_password_here

erp.jdbcUrl=jdbc:mariadb://localhost:3306/erp_db?allowPublicKeyRetrieval=true&useSSL=false
erp.username=root
erp.password=your_database_password_here
```

### 3. Build the Project

Open your terminal, navigate to the `project` directory (where `pom.xml` is located), and run:

```bash
mvn clean package
```
This will download all required dependencies and compile the code into an executable JAR file with dependencies included.

### 4. Run the Application

You can start the application by running the generated JAR file:

```bash
java -jar target/UniversityERP-1.0-SNAPSHOT-jar-with-dependencies.jar
```

*Alternatively, you can run the application directly from your IDE by executing the `edu.univ.erp.Main` class.*

## Demo

Check out the included `DemoVid.mp4` file in the root of the project to see a quick demonstration of the application's features and user flow.

## Architecture

- **UI Layer (`edu.univ.erp.UI`)**: Contains the Java Swing panels and frames for different user roles (Login, Admin, Instructor, Student).
- **Service Layer (`edu.univ.erp.service`)**: Holds business logic and communicates between the UI and Data Access Objects (DAOs).
- **Data Access Layer (`edu.univ.erp.data`)**: Manages the direct database interactions using JDBC. Uses HikariCP for connection pooling (`DbPool.java`).
- **Domain/Models (`edu.univ.erp.domain`)**: Plain Old Java Objects (POJOs) representing domain entities like `Course`, `Student`, `Instructor`, `Enrollment`, etc.

## 👥 Group Details

### Members:

* **Anmol Saluja**
* **Rishit Sansanwal**

