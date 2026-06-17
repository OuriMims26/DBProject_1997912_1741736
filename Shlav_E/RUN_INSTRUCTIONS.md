# OuriLogistic - Run Instructions

## Requirements

- IntelliJ IDEA
- JDK configured in IntelliJ: `openjdk-25`
- PostgreSQL running locally
- Database name: `LogisticsDB`
- PostgreSQL JDBC driver included at `lib/postgresql-42.7.11.jar`

## Database Connection

The application uses:

```text
jdbc:postgresql://localhost:5432/LogisticsDB
user: postgres
```

## Run From IntelliJ

1. Open the project in IntelliJ.
2. Open `Shlav_E/src/Main.java`.
3. Run the `main` method.
4. The OuriLogistic desktop window opens.

## Run From Terminal

From the repository root:

```powershell
& "$env:USERPROFILE\.jdks\openjdk-25.0.2\bin\javac.exe" -cp "Shlav_E\lib\postgresql-42.7.11.jar" -d "Shlav_E\out" "Shlav_E\src\Main.java"
& "$env:USERPROFILE\.jdks\openjdk-25.0.2\bin\java.exe" -cp "Shlav_E\out;Shlav_E\lib\postgresql-42.7.11.jar" Main
```

## Usage

- Use the left sidebar to open each table screen.
- Use `Add`, `Edit`, `Delete`, and `Refresh` for CRUD actions.
- Use `Reports` to run Stage B queries and Stage D function/procedure actions.
- IDs are hidden from the user where possible; related names are shown using joins.

