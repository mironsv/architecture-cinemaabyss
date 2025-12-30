# CinemaAbyss Events Microservice

A Java-based Events Microservice with Kafka integration for the CinemaAbyss system. This service implements the Strangler Fig pattern for event management, providing a robust event processing system with structured logging.

## Overview

The Events Microservice handles event creation and processing with full Kafka integration. It provides REST APIs for creating movie, user, and payment events, which are then processed by Kafka consumers with detailed logging.

## Features

### Event Types
- **Movie Events**: Viewed, rated, added, deleted movies
- **User Events**: Registered, updated, deleted users  
- **Payment Events**: Completed, failed, refunded payments

### Kafka Integration
- Producer: Sends events to appropriate Kafka topics
- Consumer: Processes events from all three topics
- Topics: `movie-events`, `user-events`, `payment-events`
- Group ID: `events-service-group`

### REST API Endpoints
- `POST /api/events/movie` - Create movie event
- `POST /api/events/user` - Create user event
- `POST /api/events/payment` - Create payment event
- `GET /api/events/health` - Health check

### Event Processing
- Structured logging for all events
- Error handling and retry logic
- Event metadata tracking

## Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   REST API      │───▶│   Events Service │───▶│   Kafka Topics  │
│   Controllers   │    │   (Java Spring)  │    │   movie-events  │
└─────────────────┘    └──────────────────┘    │   user-events   │
                                │               │   payment-events│
                                ▼               └─────────────────┘
                       ┌──────────────────┐              │
                       │   Event Consumer │◀─────────────┘
                       │   (Kafka Listeners)
                       └──────────────────┘
                                │
                                ▼
                       ┌──────────────────┐
                       │   Structured     │
                       │   Logging        │
                       └──────────────────┘
```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `PORT` | Application port | 8082 |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | Kafka broker URL | kafka:9092 |
| `SPRING_KAFKA_CONSUMER_GROUP_ID` | Consumer group ID | events-service-group |

### Application Properties

```properties
# Application Configuration
server.port=8082

# Kafka Configuration
spring.kafka.bootstrap-servers=kafka:9092
spring.kafka.consumer.group-id=events-service-group
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.enable-auto-commit=false

# Logging Configuration
logging.level.com.cinemaabyss.events=INFO
logging.level.com.cinemaabyss.events.service=DEBUG

# Actuator Configuration
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=always
```

## Development

### Prerequisites
- Java 17+
- Maven 3.6+
- Docker (for Kafka and testing)

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
docker build -t cinemaabyss/events-service .

# Run container
docker run -p 8082:8082 cinemaabyss/events-service
```

## API Usage

### Create Movie Event

```bash
curl -X POST http://localhost:8082/api/events/movie \
  -H "Content-Type: application/json" \
  -d '{
    "movie_id": 1,
    "title": "Inception",
    "action": "viewed",
    "user_id": 123,
    "rating": 8.8,
    "genres": ["Sci-Fi", "Action"],
    "description": "A mind-bending thriller"
  }'
```

### Create User Event

```bash
curl -X POST http://localhost:8082/api/events/user \
  -H "Content-Type: application/json" \
  -d '{
    "user_id": 123,
    "username": "john_doe",
    "email": "john@example.com",
    "action": "registered"
  }'
```

### Create Payment Event

```bash
curl -X POST http://localhost:8082/api/events/payment \
  -H "Content-Type: application/json" \
  -d '{
    "payment_id": 456,
    "user_id": 123,
    "amount": 9.99,
    "status": "completed",
    "method_type": "credit_card"
  }'
```

### Health Check

```bash
curl http://localhost:8082/api/events/health
```

## Event Processing Flow

1. **API Request**: Client sends event data to REST endpoint
2. **Validation**: Input validation using Spring annotations
3. **Event Creation**: Create Event wrapper with metadata
4. **Kafka Producer**: Send event to appropriate topic
5. **Kafka Consumer**: Consumer immediately processes the event
6. **Structured Logging**: Log event details with context

## Logging

The service provides structured logging for all events:

```
INFO  - Processing movie event: id=abc-123, type=movie, timestamp=2023-01-15T14:30:00Z, topic=movie-events, partition=0, offset=42
INFO  - Movie Event Details - Movie ID: 1, Title: 'Inception', Action: 'viewed', User ID: 123, Rating: 8.8
INFO  - Successfully processed movie event: abc-123
```

## Testing

### Unit Tests
Run unit tests with:
```bash
mvn test
```

### Integration Tests
Test with actual Kafka using docker-compose:
```bash
docker-compose up
```

### Test Endpoints
```bash
# Test movie event creation
curl -X POST http://localhost:8082/api/events/movie \
  -H "Content-Type: application/json" \
  -d '{"movie_id": 1, "title": "Test Movie", "action": "viewed"}'

# Check health
curl http://localhost:8082/api/events/health
```

## Monitoring

### Health Checks
- Application health: `GET /api/events/health`
- Actuator endpoints: `/actuator/health`, `/actuator/info`

### Logs
- Event processing logs with structured format
- Error logs with stack traces
- Kafka connection and message logs

### Metrics
- Kafka consumer lag monitoring
- Request/response metrics
- Error rate tracking

## Troubleshooting

### Common Issues

1. **Kafka Connection**: Ensure Kafka is running and accessible
2. **Topic Creation**: Topics are auto-created, but verify permissions
3. **Consumer Group**: Multiple instances use same group ID for load balancing
4. **Message Ordering**: Events within same partition maintain order

### Debug Mode
Enable debug logging:
```bash
export LOGGING_LEVEL_COM_CINEMAABYSS_EVENTS_SERVICE=DEBUG
```

### Kafka Monitoring
Use Kafka UI at `http://localhost:8090` to monitor topics and messages.

## Deployment

### Docker Compose Integration
The service is configured in `docker-compose.yml`:
```yaml
events-service:
  build:
    context: ./src/microservices/events
    dockerfile: Dockerfile
  depends_on:
    - postgres
    - kafka
  ports:
    - "8082:8082"
  environment:
    PORT: 8082
    KAFKA_BROKERS: kafka:9092
```

### Kubernetes Deployment
Use the provided Kubernetes manifests in `src/kubernetes/` directory.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Run the test suite
6. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
