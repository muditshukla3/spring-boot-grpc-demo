package com.ms;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

@GrpcService
public class BookAuthorServerService extends BookAuthorServiceGrpc.BookAuthorServiceImplBase {

    private Log log = LogFactory.getLog(BookAuthorServerService.class);
    @Override
    public void getAuthor(Author request, StreamObserver<Author> responseObserver) {

        log.trace("This is a TRACE level message");
        log.debug("This is a DEBUG level message");
        log.info("This is an INFO level message");
        log.warn("This is a WARN level message");
        log.error("This is an ERROR level message");

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
