package com.ms.service;

import com.google.protobuf.Descriptors;
import com.ms.Author;
import com.ms.Book;
import com.ms.BookAuthorServiceGrpc;
import com.ms.DummyDB;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class BookAuthorService {

    private Log log = LogFactory.getLog(BookAuthorService.class);

    //synchronous client
    @GrpcClient("grpc-service")
    BookAuthorServiceGrpc.BookAuthorServiceBlockingStub bookAuthorServiceBlockingStub;

    //asynchronous client
    @GrpcClient("grpc-service")
    BookAuthorServiceGrpc.BookAuthorServiceStub bookAuthorServiceStub;

    public Map<Descriptors.FieldDescriptor, Object> getAuthor(int authorId){
        log.trace("This is a TRACE level message");
        log.debug("This is a DEBUG level message");
        log.info("This is an INFO level message");
        log.warn("This is a WARN level message");
        log.error("This is an ERROR level message");
        Author authorRequest = Author.newBuilder().setAuthorId(authorId).build();
        Author authorResponse = bookAuthorServiceBlockingStub.getAuthor(authorRequest);
        return authorResponse.getAllFields();
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
