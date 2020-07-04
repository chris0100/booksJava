package com.books.service;

import com.books.domain.Book;
import com.books.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BookServiceImpl implements BookService {

    @Autowired
    private BookRepository bookRepositoryObj;



    @Override
    public List<Book> findAll() {
        List<Book> bookList =  (List<Book>) bookRepositoryObj.findAll();
        List<Book> activeBookList = new ArrayList<>();

        for (Book book : bookList){
            if (book.isActive()){
                activeBookList.add(book);
            }
        }
        return activeBookList;
    }


    @Override
    public Book findById(Long id) {
        return bookRepositoryObj.findById(id).get();
    }

    @Override
    public List<Book> findByCategory(String category) {
        List<Book> bookList = bookRepositoryObj.findByCategory(category);
        List<Book> activeBookList = new ArrayList<>();

        for (Book book : bookList){
            if (book.isActive()){
                activeBookList.add(book);
            }
        }
        return activeBookList;
    }


    @Override
    public List<Book> blurrySearch(String keyword) {
        List<Book> bookList = bookRepositoryObj.findByTitleContaining(keyword);
        List<Book> activeBookList = new ArrayList<>();

        for (Book book : bookList){
            if (book.isActive()){
                activeBookList.add(book);
            }
        }
        return activeBookList;
    }
}
