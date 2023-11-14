package com.ms.controller;

import com.google.protobuf.Descriptors;
import com.ms.dto.AuthorDTO;
import com.ms.service.BookAuthorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class BookAuthorController {

    private final BookAuthorService bookAuthorService;

    public BookAuthorController(BookAuthorService bookAuthorService) {
        this.bookAuthorService = bookAuthorService;
    }

    @GetMapping("/author/{id}")
    public ResponseEntity<AuthorDTO> getAuthor(@PathVariable String id){
            return ResponseEntity.ok(bookAuthorService.getAuthor(Integer.parseInt(id)));
    }

    @GetMapping("/book/{authorId}")
    public List<Map<Descriptors.FieldDescriptor, Object>> getBooksByAuthor(@PathVariable String authorId) throws InterruptedException {
        return bookAuthorService.getBooksByAuthorId(Integer.parseInt(authorId));
    }

    @GetMapping("/book/expensive")
    public Map<String, Map<Descriptors.FieldDescriptor, Object>> getExpensiveBook() throws InterruptedException {
        return bookAuthorService.getExpensiveBook();
    }

    @GetMapping("/book/author/{gender}")
    public List<Map<Descriptors.FieldDescriptor, Object>> getBooksByAuthorGender(@PathVariable String gender) throws InterruptedException {
        return bookAuthorService.getBooksByAuthorGender(gender);
    }
}
