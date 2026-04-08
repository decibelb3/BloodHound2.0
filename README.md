# BloodHound 2.0

## Overview
BloodHound 2.0 is a Java-based desktop health tracking application that allows users to monitor and analyze key health metrics such as blood pressure, cholesterol, and other measurements.

The application features multi-user authentication, persistent data storage, and alert mechanisms to help users identify potential health risks.

---

## Features

- User registration and login
- Track health measurements (blood pressure, cholesterol, etc.)
- Timestamped data entries
- Health alerts based on thresholds
- Data visualization support (charts)
- Persistent storage using MySQL
- Modern desktop UI (JavaFX)

---

## Tech Stack

- Java (JDK 17)
- JavaFX (UI)
- MySQL (Database)
- JDBC (Database connectivity)
- Maven (Build tool)

---

## Project Structure

The project is organized into logical layers:

com.bloodhound2
│
├── app # Application startup and navigation
├── config # Database configuration
├── controller # UI controllers (login, dashboard, register)
├── dao # Data access layer (database operations)
├── model # Core domain objects (User, Measurement)
├── service # Business logic, alerts, analytics
└── resources # FXML UI layouts, CSS, SQL schema


---

## Database Setup

1. Create a MySQL database:
```sql
CREATE DATABASE bloodhound2;
2. Update database credentials in: src/main/java/com/bloodhound2/config/DatabaseConfig.java
3. Run the schema file: src/main/resources/sql/schema.sql
mvn clean install
mvn javafx:run
Key Components
AuthService → Handles user authentication
MeasurementService → Manages health data
HealthAlertService → Generates risk alerts
DAO Layer → Handles database interactions
Controllers → Connect UI to backend logic
Development Notes

The repository is organized into logical commits by system layer:

Project setup and configuration
Application structure and navigation
Database configuration
Domain models
Data access layer
Business logic and services
Controllers
UI resources and schema

This structure improves readability and makes the system easier to review.

Future Improvements
Enhanced data visualization (charts/graphs)
Export functionality (CSV/PDF)
Mobile or web version
Advanced health analytics
Author

Shannon Burns
Computer Science – Texas State University
