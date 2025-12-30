# CinemaAbyss Proxy Service

A Java-based API Gateway with feature flag support for gradual migration from monolith to microservices architecture.

## Overview

The Proxy Service acts as a Strangler Fig pattern implementation, routing requests between the existing monolith and new microservices based on feature flags. It provides:

- **Feature Flag Support**: Gradual migration with configurable traffic distribution
- **Request Routing**: Intelligent routing based on path patterns
- **Health Monitoring**: Built-in health checks for all services
- **Circuit Breaker**: Resilient fallback mechanisms

## Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Client App    │───▶│   API Gateway    │───▶│  Monolith (8080)│
└─────────────────┘    │   (Java Spring)  │    └─────────────────┘
                       └──────────────────┘
                                │
                                ▼
                       ┌──────────────────┐
                       │ Movies Service   │
                       │   (8081)         │
                       └──────────────────┘
                                │
                                ▼
                       ┌──────────────────┐
                       │ Events Service   │
                       │   (8082)         │
                       └──────────────────┘
```

## Features

### Feature Flags
- **GRADUAL_MIGRATION**: Enable/disable gradual migration (boolean)
- **MOVIES_MIGRATION_PERCENT**: Percentage of traffic to route to movies microservice (0-100)

### Routing Logic
- `/api/movies/*` → Movies Microservice (with feature flag logic)
- `/api/events/*` → Events Microservice
- `/api/users/*`, `/api/payments/*`, `/api/subscriptions/*` → Monolith

### Health Monitoring
- `/health` - Proxy service health check
- Automatic service availability detection

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `PORT` | Application port | 8000 |
| `CINEMAABYSS_MONOLITH_URL` | Monolith service URL | http://monolith:8080 |
| `CINEMAABYSS_MOVIES_SERVICE_URL` | Movies service URL | http://movies-service:8081 |
| `CINEMAABYSS_EVENTS_SERVICE_URL` | Events service URL | http://events-service:8082 |
| `CINEMAABYSS_GRADUAL_MIGRATION` | Enable gradual migration | false |
| `CINEMAABYSS_MOVIES_MIGRATION_PERCENT` | Migration percentage for movies | 0 |

### Docker Compose Integration

The proxy service is configured in `docker-compose.yml`:

```yaml
proxy-service:
  build:
    context: ./src/microservices/proxy
    dockerfile: Dockerfile
  environment:
    PORT: 8000
    CINEMAABYSS_MONOLITH_URL: http://monolith:8080
    CINEMAABYSS_MOVIES_SERVICE_URL: http://movies-service:8081
    CINEMAABYSS_EVENTS_SERVICE_URL: http://events-service:8082
    CINEMAABYSS_GRADUAL_MIGRATION: "true"
    CINEMAABYSS_MOVIES_MIGRATION_PERCENT: "50"
```

## Development

### Prerequisites
- Java 21+
- Maven 3.6+
- Docker (for containerization)

### Building

```bash
# Build the application
mvn clean package

# Run tests
mvn test

# Run the application
mvn spring-boot:run
```

### Docker Build

```bash
# Build Docker image
docker build -t cinemaabyss/proxy-service .

# Run container
docker run -p 8000:8000 cinemaabyss/proxy-service
```

## Testing

### Unit Tests
Run unit tests with:
```bash
mvn test
```

### Integration Tests
Test with actual microservices using docker-compose:
```bash
docker-compose up
```

### Feature Flag Testing

1. **Disable Migration**:
   ```bash
   export CINEMAABYSS_GRADUAL_MIGRATION=false
   ```

2. **Enable 50% Migration**:
   ```bash
   export CINEMAABYSS_GRADUAL_MIGRATION=true
   export CINEMAABYSS_MOVIES_MIGRATION_PERCENT=50
   ```

3. **Enable 100% Migration**:
   ```bash
   export CINEMAABYSS_GRADUAL_MIGRATION=true
   export CINEMAABYSS_MOVIES_MIGRATION_PERCENT=100
   ```

## API Endpoints

### Health Check
- `GET /health` - Proxy service health status

### Proxy Endpoints
All `/api/*` requests are proxied to appropriate backend services:
- `GET /api/movies` - List movies (routed based on feature flags)
- `GET /api/users` - List users (always to monolith)
- `GET /api/events` - List events (always to events service)

## Monitoring

### Logs
The application uses structured logging with SLF4J. Configure log levels in `application.properties`:

```properties
logging.level.com.cinemaabyss.proxy=INFO
logging.level.com.cinemaabyss.proxy.service=DEBUG
```

### Health Checks
- Application health: `GET /health`
- Service availability is automatically monitored

## Troubleshooting

### Common Issues

1. **Service Unavailable**: Check that backend services are running
2. **Feature Flags Not Working**: Verify environment variables are set correctly
3. **CORS Issues**: Configure CORS settings in application properties

### Debug Mode
Enable debug logging for detailed request/response information:
```bash
export LOGGING_LEVEL_COM_CINEMAABYSS_PROXY_SERVICE=DEBUG
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Run the test suite
6. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
