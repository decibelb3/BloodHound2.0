# BloodHound 2.0

## Overview
BloodHound 2.0 is a Java desktop health tracking application for recording and reviewing key metrics such as blood pressure, cholesterol, and weight.

It includes multi-user authentication, MySQL persistence, health alerts, and chart-based trend visualization in a JavaFX UI.

## Features
- User registration and login
- Separate blood pressure and cholesterol entry workflows
- Timestamped health entries with edit/delete support
- Health alerts based on threshold logic
- Dashboard summary that combines latest non-null BP and cholesterol values
- Trend charts for blood pressure, cholesterol, and weight
- Persistent storage with MySQL

## Tech Stack
- Java 17
- JavaFX
- MySQL
- JDBC
- Maven

## Project Structure
The project is organized into layered packages under `com.bloodhound2`:

- `app` - application bootstrap and navigation
- `config` - database configuration
- `controller` - JavaFX controllers (`login`, `register`, `dashboard`)
- `dao` - data access logic
- `model` - domain models (`User`, `Measurement`)
- `service` - business logic (authentication, measurements, alerts, chart data, session)

Resources live under `src/main/resources`:
- `fxml` - JavaFX views
- `styles` - CSS styles
- `sql` - schema scripts

## Database Setup
1. Create the database:

```sql
CREATE DATABASE bloodhound2;
```

2. Update credentials in:
`src/main/java/com/bloodhound2/config/DatabaseConfig.java`

3. Apply schema:
`src/main/resources/sql/schema.sql`

## Build and Run
```bash
mvn clean install
mvn javafx:run
```

## Key Components
- `AuthService` - user authentication and registration
- `MeasurementService` - measurement create/read/update/delete workflows
- `HealthAlertService` - threshold-based health alerts
- `MeasurementChartDataService` - chart series construction
- `UserDao` / `MeasurementDao` - persistence layer

## Notes
- Blood pressure and cholesterol can be recorded at different times.
- The table can display merged rows for close-timestamp entries while storage remains separate.
- The health summary uses the latest available non-null values across recent entries.

## Future Improvements
- Enhanced chart controls and filtering
- Data export (CSV/PDF)
- Mobile/web companion clients
- Broader analytics and insights

## Author
Shannon Burns  
Computer Science - Texas State University
