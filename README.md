# Alerting System for Monitoring Failed API Requests

## Overview
This is a Spring Boot application designed to monitor and track failed API requests and notify, with features including:
- Request validation
- Failed request logging
- IP-based alert system
- Email notifications for suspicious activities

### Prerequisites
- Java 17, MongoDB, Maven, Gmail account (for SMTP email alerts)

### Technology Stack
- Spring Boot 3.2.1, MongoDB, Java Mail Sender, Lombok

## Configuration Steps

### 1. Clone the Repository
```bash
git clone https://github.com/vivekanand-vr/alertingSystem.git
cd alertingSystem
```

### 2. Configure Server Values
- Create `application.properties`
- Add threshold and email address
- Add number of threads for handling traffic

```properties
# Alert Configuration
alert.threshold=5
alert.email=admin@email.com

# Server Configuration for High Traffic
server.tomcat.threads.max=200
server.tomcat.max-connections=10000
```

### 3. Configure MongoDB
- Open `src/main/resources/application.properties`
- Ensure MongoDB is running locally
- Default connection: `mongodb://localhost:27017/<your-db-name>`

### 4. Configure Email Settings
1. Open `src/main/resources/application.properties`
2. Update email configuration:
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

### 5. Generate App Password for Gmail
- Go to Google Account
- Security > 2-Step Verification
- App Passwords > Select App (Mail) > Generate
- Use generated password in `application.properties`

### 5. Build and Run the Application
```bash
# Build the project
mvn clean package

# Run the application
mvn spring-boot:run
```

## API Endpoints

### 1. Submit Endpoint
- **URL:** `/api/submit`
- **Method:** POST
- **Required Headers:**
  - `Authorization: Bearer FIXED_SECRET_TOKEN_2024`
  - `X-Custom-Header: SomeValue`
- **Required Params:**
  - `clientId`
  - `version`
- **Request Body:** JSON with `name` and `description`

---

### 2. Metrics Endpoint
- **URL:** `/api/metrics`
- **Method:** GET
- Returns list of failed requests

## Example Request
```bash
curl -X POST "http://localhost:8080/api/submit?clientId=123&version=1.0" \
     -H "Authorization: Bearer FIXED_SECRET_TOKEN_2024" \
     -H "X-Custom-Header: SomeValue" \
     -H "Content-Type: application/json" \
     -d '{"name":"John Doe","description":"Sample Description"}'
```

## Logging
- Failed requests are logged in MongoDB
- Email alerts sent when failure threshold is reached

## Email Notification Snapshot
![image](https://github.com/user-attachments/assets/175dfee7-bc0b-44f2-891f-893ce7af1996)


## Customization
Modify `application.properties` to adjust:
- Alert threshold
- Alert email
- MongoDB connection
- SMTP settings

## Troubleshooting
- Ensure MongoDB is running
- Check SMTP credentials
- Verify network configurations
