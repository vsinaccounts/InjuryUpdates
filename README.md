# Injury Feed Service

A Spring Boot application that consumes injury updates from SpankOdds RabbitMQ feed and provides a REST API for accessing the data.

## Features

- **Real-time Data Ingestion**: Connects to SpankOdds RabbitMQ server and consumes injury updates in real-time
- **Data Storage**: Stores injury updates in an H2 database (easily configurable for other databases)
- **REST API**: Provides comprehensive REST endpoints for accessing injury data
- **Cloud Ready**: Dockerized and ready for cloud deployment
- **CORS Enabled**: Frontend-friendly with CORS support

## API Endpoints

### Core Endpoints
- `GET /api/injury-updates` - Get all injury updates
- `GET /api/injury-updates/recent?hours=24` - Get recent updates (default: 24 hours)
- `GET /api/injury-updates/latest-per-player` - Get the latest update for each player
- `GET /api/injury-updates/stats` - Get statistics about injury updates
- `GET /api/injury-updates/health` - Health check endpoint

### Filter Endpoints
- `GET /api/injury-updates/league/{league}` - Get updates by league (e.g., NCAAB, NBA)
- `GET /api/injury-updates/team/{team}` - Get updates by team name
- `GET /api/injury-updates/player/{playerName}` - Get updates by player name
- `GET /api/injury-updates/status/{status}` - Get updates by status
- `GET /api/injury-updates/rotation/{rotation}` - Get updates by rotation number
- `GET /api/injury-updates/league/{league}/team/{team}` - Get updates by league and team

### Search Endpoint
- `GET /api/injury-updates/search` - Advanced search with multiple filters
  - Query parameters: `league`, `team`, `playerName`, `status`, `rotation`, `hoursBack`

## Data Structure

Each injury update contains:
```json
{
  "id": 1,
  "status": "DOWNGRADED TO DOUBTFUL",
  "injuryType": "Upper Body",
  "position": "G",
  "team": "Siena",
  "rotation": 735,
  "playerName": "Sean Durugordon",
  "league": "NCAAB",
  "receivedAt": "2024-01-15T10:30:00"
}
```

## Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher
- Docker (for containerized deployment)

### Running Locally

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd injury-feed-service
   ```

2. **Build and run**:
   ```bash
   ./mvnw clean spring-boot:run
   ```

3. **Access the application**:
   - API: http://localhost:8080/api/injury-updates
   - Health Check: http://localhost:8080/api/injury-updates/health
   - H2 Console: http://localhost:8080/h2-console

### Docker Deployment

1. **Build Docker image**:
   ```bash
   docker build -t injury-feed-service .
   ```

2. **Run container**:
   ```bash
   docker run -p 8080:8080 injury-feed-service
   ```

## Cloud Deployment

### Heroku
1. Create a new Heroku app
2. Connect your GitHub repository
3. Deploy from the main branch
4. The application will be available at your Heroku app URL

### AWS/Google Cloud
1. Push the Docker image to your cloud registry
2. Deploy using your cloud platform's container service
3. Ensure port 8080 is exposed

## Configuration

### Database Configuration
The application uses H2 in-memory database by default. For production, update `application.properties`:

```properties
# PostgreSQL example
spring.datasource.url=jdbc:postgresql://localhost:5432/injurydb
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

### RabbitMQ Configuration
RabbitMQ credentials are configured in `InjuryUpdateService.java`. For production, consider using environment variables:

```java
private static final String USERNAME = System.getenv("RABBITMQ_USERNAME");
private static final String PASSWORD = System.getenv("RABBITMQ_PASSWORD");
```

## Frontend Integration

Example JavaScript code to fetch injury updates:

```javascript
// Get all recent injury updates
fetch('http://your-api-url/api/injury-updates/recent?hours=24')
  .then(response => response.json())
  .then(data => {
    console.log('Injury updates:', data);
  });

// Get updates for a specific league
fetch('http://your-api-url/api/injury-updates/league/NCAAB')
  .then(response => response.json())
  .then(data => {
    console.log('NCAAB updates:', data);
  });
```

## Monitoring

- **Health Check**: `/api/injury-updates/health`
- **Statistics**: `/api/injury-updates/stats`
- **Spring Actuator**: `/actuator/health` (enabled in production)

## Development

### Adding New Features
1. Update the `InjuryUpdate` entity if new fields are needed
2. Add repository methods in `InjuryUpdateRepository`
3. Implement business logic in `InjuryUpdateService`
4. Create REST endpoints in `InjuryUpdateController`

### Testing
```bash
./mvnw test
```

## License

This project is licensed under the MIT License.

## Support

For questions or issues, please open a GitHub issue or contact the development team. 