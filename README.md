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

#### Running grpc-server
Go to grpc-servce and run the following command:
```mvn spring-boot:run```

#### Running grpc-client
This module calls the gRPC service and exposes the REST API's for end client. Go to grpc-client and run the following command ```mvn spring-boot:run``` to start the grpc client service. The rest api's can be accessed via swagger ui.
***http://localhost:8080/swagger-ui/***

### Running application in Kubernetes

#### Running grpc-server
Go to grpc-service folder and run the following command:
```
kubectl apply -f deployment.yml
```
Run the following command to delete the deployment and service
```
kubectl delete deployment grpc-service
kubectl delete services grpc-service
```

The service object of grpc-service is a headless service. With headless service we can achieve client side load balancing with gRPC. More can be read here about gRPC load balancing [here](https://grpc.io/blog/grpc-load-balancing/)

#### Running grpc-client
Go to grpc-client folder and run the following command:
```
kubectl apply -f deployment.yml
```
Run the following command to delete the deployment and service
```
kubectl delete deployment grpc-client
kubectl delete services grpc-client-nodeport
```

Access the grpc client application by going to http://localhost:31111/swagger-ui/
