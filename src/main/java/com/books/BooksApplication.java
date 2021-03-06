package com.books;

import com.books.domain.User;
import com.books.domain.security.Role;
import com.books.domain.security.UserRole;
import com.books.service.UserService;
import com.books.utility.SecurityUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashSet;
import java.util.Set;


@SpringBootApplication
public class BooksApplication implements CommandLineRunner {

    @Autowired
    private UserService userService;

    public static void main(String[] args) {
        SpringApplication.run(BooksApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        User user2 = new User();
        user2.setFirstName("John");
        user2.setLastName("Adams");
        user2.setUsername("j");
        user2.setPassword(SecurityUtility.passwordEncoder().encode("p"));
        user2.setEmail("JAdams@gmail.com");
        Set<UserRole> userRoles = new HashSet<>();
        Role role2= new Role();
        role2.setRoleId(1);
        role2.setName("ROLE_USER");
        userRoles.add(new UserRole(user2, role2));

        userService.createUser(user2, userRoles);
    }
}
