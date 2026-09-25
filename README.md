// Patient Service

// // Purpose
A Spring boot rest api for patient

// // Tehcnology
- Java 21
- Spring Boot
- Maven
- In-mem repo

// // APIs
- POST /patients
- GET /patients
- Get /patients/{id}

// sql conn
- psql -h localhost -U patientapp -d patientdb

// Docker build
- docker build -t patient-service:local .
// Docker run
- docker run --rm \
    -p 8080:8080 \
    --env-file .dockerenv.local \
    -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/patientdb \
    -e SPRING_KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:9092 \
    -e SPRING_DATA_REDIS_HOST=host.docker.internal \
    patient-service:local


// using compose
- docker compose up --build
