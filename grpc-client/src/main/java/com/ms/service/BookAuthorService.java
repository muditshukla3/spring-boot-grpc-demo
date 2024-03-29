package com.ms.service;

import com.google.protobuf.Descriptors;
import com.ms.Author;
import com.ms.Book;
import com.ms.BookAuthorServiceGrpc;
import com.ms.DummyDB;
import com.ms.dto.AuthorDTO;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.sql.SQLOutput;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class BookAuthorService {

    //synchronous client
    @GrpcClient("grpc-service")
    BookAuthorServiceGrpc.BookAuthorServiceBlockingStub bookAuthorServiceBlockingStub;

    //asynchronous client
    @GrpcClient("grpc-service")
    BookAuthorServiceGrpc.BookAuthorServiceStub bookAuthorServiceStub;
    @CircuitBreaker(name = "circuit-breaker-author", fallbackMethod = "getAuthorFallBack")
    public AuthorDTO getAuthor(int authorId){
        Author authorRequest = Author.newBuilder().setAuthorId(authorId).build();
        Author response = bookAuthorServiceBlockingStub.getAuthor(authorRequest);
        AuthorDTO authorDTO = new AuthorDTO(response.getAuthorId(),
                                            response.getFirstName(),
                                            response.getLastName(),
                                            response.getGender(),
                                            response.getBookId());
        return authorDTO;
    }

    private AuthorDTO getAuthorFallBack(int authorId, Exception e){
        log.info("Handled exception "+e.getMessage());
        return new AuthorDTO(authorId, "","","", -1);
    }

    private AuthorDTO getAuthorFallBack(int authorId, CallNotPermittedException e){
        log.info("Handled CallNotPermitted "+e.getMessage());
        return new AuthorDTO(authorId, "","","", -1);
    }
    public List<Map<Descriptors.FieldDescriptor, Object>> getBooksByAuthorId(int authorId) throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        Author authorRequest = Author.newBuilder().setAuthorId(authorId).build();
        List<Map<Descriptors.FieldDescriptor, Object>> response = new ArrayList<>();
                bookAuthorServiceStub.getBookByAuthor(authorRequest, new StreamObserver<Book>() {
                    @Override
                    public void onNext(Book book) {
                        response.add(book.getAllFields());
                        System.out.println("Got book "+book.getTitle());
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        countDownLatch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        countDownLatch.countDown();
                    }
                });
                boolean await = countDownLatch.await(1, TimeUnit.MINUTES);
                return await ? response: Collections.emptyList();
    }

    public Map<String, Map<Descriptors.FieldDescriptor, Object>> getExpensiveBook() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        Map<String, Map<Descriptors.FieldDescriptor, Object>> response = new HashMap<>();
        StreamObserver<Book> responseObserver = bookAuthorServiceStub.getExpensiveBooks(new StreamObserver<Book>() {
            @Override
            public void onNext(Book book) {
                response.put("Expensive Book", book.getAllFields());
            }

            @Override
            public void onError(Throwable throwable) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        DummyDB.getBooksFromTempDb()
                .forEach(responseObserver::onNext);
        responseObserver.onCompleted();
        boolean await = latch.await(1, TimeUnit.MINUTES);
        return await ? response : Collections.emptyMap();
    }

    public List<Map<Descriptors.FieldDescriptor, Object>> getBooksByAuthorGender(String gender) throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        List<Map<Descriptors.FieldDescriptor, Object>> response = new ArrayList<>();
        StreamObserver<Book> bookStreamObserver =
                bookAuthorServiceStub.getBookByAuthorGender(new StreamObserver<Book>() {
            @Override
            public void onNext(Book book) {
                response.add(book.getAllFields());
            }

            @Override
            public void onError(Throwable throwable) {
                countDownLatch.countDown();
            }

            @Override
            public void onCompleted() {
                countDownLatch.countDown();
            }
        });
        DummyDB.getAuthorsFromTempDb()
                .stream()
                .filter(author -> author.getGender().equalsIgnoreCase(gender))
                .forEach(author ->
                        bookStreamObserver.onNext(Book.newBuilder().setAuthorId(author.getAuthorId()).build()));
        bookStreamObserver.onCompleted();

        boolean await = countDownLatch.await(1, TimeUnit.MINUTES);
        return await ? response : Collections.emptyList();
    }
}
