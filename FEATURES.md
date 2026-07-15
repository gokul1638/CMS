# Cloud Management System - Feature Overview

## Project Summary
This project is a Spring Boot REST API for managing cloud resources such as compute instances, storage, and networking components. The current implementation is a basic MVP with health checking, resource listing, and resource creation.

## Base URL
- Local server: http://localhost:8081

## Current Features

### 1. Health Check
A simple endpoint to verify that the application is running.

- Purpose: check service availability
- Method: GET
- Endpoint: /health
- Response: plain text message

Example:
```bash
curl http://localhost:8081/health
```

Example response:
```text
Cloud Management System is running
```

### 2. List All Cloud Resources
Retrieves all stored cloud resources from the database.

- Purpose: view all existing resources
- Method: GET
- Endpoint: /api/resources
- Authentication: required
- Response: JSON array of resources

Example:
```bash
curl -u username:password http://localhost:8081/api/resources
```

### 3. Create a Cloud Resource
Adds a new cloud resource to the database.

- Purpose: register a new resource
- Method: POST
- Endpoint: /api/resources
- Authentication: required
- Request body: JSON object
- Response: created resource object

Example request body:
```json
{
  "name": "EC2-Instance",
  "type": "Compute",
  "status": "Running"
}
```

Example:
```bash
curl -u username:password -X POST http://localhost:8081/api/resources \
  -H "Content-Type: application/json" \
  -d '{"name":"EC2-Instance","type":"Compute","status":"Running"}'
```

## Resource Structure
Each resource contains the following fields:

- id: auto-generated unique identifier
- name: resource name
- type: resource category such as Compute, Storage, or Network
- status: current state such as Running, Stopped, or Pending

## How the Application Works

1. A client sends an HTTP request to one of the REST endpoints.
2. The controller receives the request.
3. The controller calls the service layer.
4. The service layer uses the repository to interact with the database.
5. The result is returned as JSON to the client.

This flow is:
```text
Controller -> Service -> Repository -> MySQL Database
```

## Security
Security is enabled for most endpoints.

- Public endpoints:
  - /health
  - /actuator/health
  - /error
- Protected endpoints:
  - /api/resources

The app uses Spring Security with HTTP Basic Authentication.

## Data Storage
The application uses:

- MySQL database
- Spring Data JPA
- Hibernate for ORM

Database settings are configured in the application properties file.

## Current Status
This is a basic backend system with the following implemented functionality:
- application health check
- list resources
- create resources
- persistence in MySQL
- basic security protection

## Suggested Next Improvements
Possible future enhancements include:
- update and delete resource endpoints
- search and filter by type/status
- pagination and sorting
- user management and role-based access
- validation for request payloads
- API documentation with Swagger/OpenAPI
