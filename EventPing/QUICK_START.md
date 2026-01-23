# Quick Start Guide - EventPing Application

## The Problem
You're experiencing PostgreSQL 18 incompatibility with Flyway. Here's how to verify your setup.

## Step-by-Step Solution

### Step 1: Verify Database is Running
```bash
# Check if PostgreSQL is running
pg_isready

# Expected output: localhost:5432 - accepting connections
```

### Step 2: Create the Database (if not exists)
```sql
-- Open psql as postgres user
psql -U postgres

-- Create database  
CREATE DATABASE "EventPing";

-- Verify it exists
\l

--Exit
\q
```

### Step 3: Test Manual Connection
```bash
psql -U postgres -d EventPing

# If successful, you'll see:
# EventPing=#
```

### Step 4: Run Application in Debug Mode
```bash
.\mvnw.cmd spring-boot:run -Ddebug
```

## Current Configuration

**Database:** EventPing  
**User:** postgres  
**Password:** abasthomas  
**Flyway:** Disabled (using Hibernate DDL)  
**DDL Mode:** update (auto-creates tables)

## What Should Happen

When you run the app, you should see:
```
✅ Hibernate: create table users ...
✅ Hibernate: create table plans ...
✅ Hibernate: create table events ...
✅ Hibernate: create table participants ...
✅ Hibernate: create table reminders ...
✅ Database already seeded with 2 plans via Flyway
✅ Started EventPingApplication
```

## If It Still Fails

1. Check if database "EventPing" exists (case-sensitive!)
2. Verify postgres user can connect
3. Look at the full error in console
4. Try running in your IDE's debug/run console instead of Maven

## Alternative: Use Flyway with Correct Version

If you want to use Flyway, update `pom.xml`:
```xml
<flyway.version>10.21.0</flyway.version>
```

Then in `application.properties`:
```properties
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
```
