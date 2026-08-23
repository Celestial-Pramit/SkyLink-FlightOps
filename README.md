# SkyLink FlightOps

SkyLink FlightOps is a Spring Boot 4 application for managing flights, aircraft,
customers, bookings, reports, and role-based operations.

## Requirements

- Java 17
- MySQL 8 for local development
- Git
- macOS or Windows

Maven is not required separately. Use the Maven wrapper included in the project.

## Clone

```bash
git clone https://github.com/Celestial-Pramit/SkyLink-FlightOps.git
cd SkyLink-FlightOps
```

On macOS or Linux, make the Maven wrapper executable once:

```bash
chmod +x mvnw
```

## MySQL Setup

Create the database and local application user in MySQL:

```sql
CREATE DATABASE skylink_db;
CREATE USER 'flightadmin'@'localhost' IDENTIFIED BY 'choose-a-local-password';
GRANT ALL PRIVILEGES ON skylink_db.* TO 'flightadmin'@'localhost';
FLUSH PRIVILEGES;
```

The application reads database credentials from environment variables. Do not
commit passwords to Git.

macOS or Linux:

```bash
export DB_USERNAME=flightadmin
export DB_PASSWORD='choose-a-local-password'
```

Windows PowerShell:

```powershell
$env:DB_USERNAME="flightadmin"
$env:DB_PASSWORD="choose-a-local-password"
```

Use the same database name and credentials when working on both computers.

## Upload Directories

Create these directories from the project root if they do not already exist:

```text
uploads/aircraft/
uploads/banners/
uploads/customers/
uploads/misc/
```

Uploaded files are intentionally excluded from Git and must be copied separately
between computers if they are needed.

## Run the Application

macOS or Linux:

```bash
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Open http://localhost:8088.

## Run Tests

Tests use an in-memory H2 database and do not require MySQL:

macOS or Linux:

```bash
./mvnw clean test
```

Windows PowerShell:

```powershell
.\mvnw.cmd clean test
```

## Git Workflow

Before starting work:

```bash
git fetch origin
git pull --ff-only origin master
```

Use a feature branch for changes:

```bash
git switch -c feature/your-feature-name
```

Before pushing, run tests and inspect the changes:

```bash
./mvnw clean test
git status
git diff
git add path/to/intended/files
git commit -m "describe the change"
git push -u origin feature/your-feature-name
```

Never commit `.env`, `application-prod.yml`, passwords, `target/`, `.idea/`,
logs, or uploaded files.
