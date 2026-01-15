# MonDiDong Backend - Spring Boot API

E-commerce backend API built with Spring Boot, MySQL, and Cloudinary.

## 🔐 Security Setup

**IMPORTANT:** Never commit sensitive credentials to Git!

### 1. Create Configuration Files

Copy the example files and fill in your actual credentials:

```bash
# Copy application.properties template
cp src/main/resources/application.properties.example src/main/resources/application.properties

# Copy CloudinaryConfig template
cp src/main/java/com/example/backend/config/CloudinaryConfig.java.example src/main/java/com/example/backend/config/CloudinaryConfig.java
```

### 2. Update Credentials

Edit the following files with your actual credentials:
- `src/main/resources/application.properties`
- `src/main/java/com/example/backend/config/CloudinaryConfig.java`

**These files are already in `.gitignore` and will NOT be committed.**

## 🚀 Getting Started

### Prerequisites
- Java 17+
- MySQL 8.0+
- Maven 3.6+

### Installation

1. Clone the repository
2. Set up configuration files (see Security Setup above)
3. Create MySQL database:
   ```sql
   CREATE DATABASE didong2;
   ```
4. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

## 📝 API Documentation

Once running, access Swagger UI at:
```
http://localhost:8080/swagger-ui.html
```

## 🔑 Required Credentials

You need to obtain the following:
- **MySQL**: Database credentials
- **Cloudinary**: Cloud name, API key, API secret
- **VNPay**: TMN code, hash secret (for payment)
- **Gemini AI**: API key (for chatbot)
- **Gmail**: App password (for email notifications)

## ⚠️ Security Notes

- Never commit `application.properties` or `CloudinaryConfig.java`
- Use `.example` files as templates
- Rotate credentials regularly
- Use environment variables in production
