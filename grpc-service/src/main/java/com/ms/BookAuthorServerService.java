package com.ms;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.ArrayList;
import java.util.List;

@GrpcService
@Slf4j
public class BookAuthorServerService extends BookAuthorServiceGrpc.BookAuthorServiceImplBase {
    @Override
    public void getAuthor(Author request, StreamObserver<Author> responseObserver) {
        log.info("GetAuthor request... {}", request);
        DummyDB.getAuthorsFromTempDb().
                stream().filter(author -> author.getAuthorId() == request.getAuthorId())
                .findFirst().ifPresent(responseObserver::onNext);
        responseObserver.onCompleted();
    }

    @Override
    public void getBookByAuthor(Author request, StreamObserver<Book> responseObserver) {
        DummyDB.getBooksFromTempDb()
                .stream()
                .filter(book -> book.getAuthorId() == request.getAuthorId())
                .forEach(responseObserver::onNext);

        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<Book> getExpensiveBooks(StreamObserver<Book> responseObserver) {
        return new StreamObserver<Book>() {
            Book expensiveBook = null;
            float priceTrack = 0;
            @Override
            public void onNext(Book book) {
                if(book.getPrice() > priceTrack){
                    priceTrack=book.getPrice();
                    expensiveBook = book;
                }
            }

            @Override
            public void onError(Throwable throwable) {
                responseObserver.onError(throwable);
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(expensiveBook);
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<Book> getBookByAuthorGender(StreamObserver<Book> responseObserver) {
        return new StreamObserver<Book>() {
            List<Book> books = new ArrayList<>();
            @Override
            public void onNext(Book book) {
                DummyDB.getBooksFromTempDb()
                        .stream()
                        .filter(booksFromDb -> book.getAuthorId() == booksFromDb.getAuthorId())
                        .forEach(books::add);
            }

            @Override
            public void onError(Throwable throwable) {
                    responseObserver.onError(throwable);
            }

            @Override
            public void onCompleted() {
                books.forEach(responseObserver::onNext);
                responseObserver.onCompleted();
            }
        };
    }
}
