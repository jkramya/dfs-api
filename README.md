# Distrubuted File Sharing System
The following was discovered as part of building this project:

* The original package name 'com.basktpay.dfs-api' is invalid and this project uses 'com.basktpay.dfsapi' instead.
# Tools and Technologies
### Java 17 
### Spring Boot 2.7.17 
### Swagger 1.7.0 
# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/3.2.0/maven-plugin/reference/html/)

### Application endpoints
 The application is developed to perform the read and write operation of the internal shared file.

GET - http://localhost:8080/file

POST - http://localhost:8080/file?app_key={api.key}

# Implementation
### Front-End
Implemented the basic HTML and JavaScript page to handle GET and POST operations.
For the write operation, the API key is implemented with basic authentication, and it is configured in the application.properties file. The file path and file name are also configured in application.properties.
### SharedFile Class
The SharedFile class is a component responsible for managing a shared file within the DFS API application. It facilitates concurrent read and write operations on the shared file, ensuring data integrity and reliability. This class is a crucial part of the distributed file system architecture implemented in the application.
#### Purpose
The primary purpose of the SharedFile class is to:
Read: Retrieve the content of the shared file.
Write: Add new data to the shared file asynchronously, ensuring smooth and efficient operations.
#### Features
Synchronous file reading.
Asynchronous file writing with a dedicated writer thread.
Graceful shutdown mechanism for clean application termination.
#### Key Components
File Reading: Supports synchronous reading of the shared file's content.
File Writing: Asynchronously writes data to the shared file, ensuring minimal impact on application performance.
Error Handling: Detects and handles file-related errors, providing robustness to the system.

### Fault Tolerance
Implemented the circuit breaker mechanism for the write operation.

# Junit and Integration Test

Wrote JUnit tests for the controller class and shared file class.
Integration tests were created to check the endpoints.

# API Documentation
http://localhost:8080/swagger-ui/index.html
