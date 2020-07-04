package com.books.controller;

import com.books.domain.Book;
import com.books.domain.User;
import com.books.service.BookService;
import com.books.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
public class SearchController {

    @Autowired
    private UserService userServiceObj;

    @Autowired
    private BookService bookServiceObj;

    @GetMapping("/searchByCategory")
    public String searchByCategory(@RequestParam("category") String category, Model model, Principal principal){
        if (principal != null){
            String username = principal.getName();
            User user = userServiceObj.findByUsername(username);
            model.addAttribute("user",user);
        }

        String classActiveCategory = "active" + category;
        classActiveCategory = classActiveCategory.replaceAll("\\s+","");
        classActiveCategory = classActiveCategory.replaceAll("&","");
        model.addAttribute(classActiveCategory, true);

        List<Book> bookList = bookServiceObj.findByCategory(category);

        if (bookList.isEmpty()){
            model.addAttribute("emptyList",true);
            return "bookshelf";
        }

        model.addAttribute("bookList",bookList);
        return "bookshelf";
    }


    @PostMapping("/searchBook")
    public String searchBook(@ModelAttribute("keyword") String keyword, Principal principal, Model model){
        if (principal != null){
            String username = principal.getName();
            User user = userServiceObj.findByUsername(username);
            model.addAttribute("user",user);
        }

        List<Book> bookList = bookServiceObj.blurrySearch(keyword);

        if (bookList.isEmpty()){
            model.addAttribute("emptyList", true);
            System.out.println("retorna la lista vacia");
            return "bookshelf";
        }
        System.out.println("retorna la lista completa: " + bookList.size());
        model.addAttribute("bookList",bookList);
        return "bookshelf";
    }
}












