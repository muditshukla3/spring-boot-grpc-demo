## Introduction

This is a sample project demonstrating gRPC with Spring Boot. This project demonstrates all 4 types of gRPC service implementation.
- unary
- client-streaming
- server-streaming
- bi-directional

This is multi-module maven project containing gRPC service, gRPC client and module containing proto files.

### Modules
- proto
- grpc-service
- grpc-client

#### proto module
This module contains proto file and DummyDB file for service and client demonstration. To generate the server and client code issue the following command from proto folder:

```mvn compile```

This command will generate the server and client code. As proto is a maven module it has been included as a dependency in other two modules which are grpc-service and grpc-client.

#### grpc-client
This module calls the gRPC service and exposes the REST API's for end client. The rest api's can be accessed via swagger ui.
***http://localhost:8080/swagger-ui/***
