package com.books.controller;

import com.books.domain.Book;
import com.books.domain.CartItem;
import com.books.domain.ShoppingCart;
import com.books.domain.User;
import com.books.service.BookService;
import com.books.service.CartItemService;
import com.books.service.ShoppingCartService;
import com.books.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private UserService userServiceObj;

    @Autowired
    private CartItemService cartItemServiceObj;

    @Autowired
    private ShoppingCartService shoppingCartServiceObj;

    @Autowired
    private BookService bookServiceObj;



    @RequestMapping("/cart")
    public String shoppingCart(Model model, Principal principal){
        User user = userServiceObj.findByUsername(principal.getName());
        ShoppingCart shoppingCart = user.getShoppingCart();

        List<CartItem> cartItemList = cartItemServiceObj.findByShoppingCart(shoppingCart);
        shoppingCartServiceObj.updateShoppingCart(shoppingCart);

        model.addAttribute("cartItemList", cartItemList);
        model.addAttribute("shoppingCart", shoppingCart);
        model.addAttribute("user",user);

        return "shoppingCart";
    }


    @PostMapping("/addItem")
    public String addItem(@ModelAttribute("book") Book book, @ModelAttribute("qty") String qty,
                          Model model, Principal principal){
        User user = userServiceObj.findByUsername(principal.getName());
        book = bookServiceObj.findById(book.getId());
        System.out.println("el id del libro es: " + book.getId());

        //si la cantidad solicitada es mayor a la cantidad en stock envia error.
        if (Integer.parseInt(qty) > book.getInStockNumber()){
            model.addAttribute("notEnoughStock", true);
            return "redirect:/bookDetail?id="+book.getId();
        }

        CartItem cartItem = cartItemServiceObj.addBookToCartItem(book, user, Integer.parseInt(qty));

        //flash attributte deberia de utilizarse en esta ocasion
        model.addAttribute("addBookSuccess", true);

        return "redirect:/bookDetail?id="+book.getId();
    }


    @PostMapping("/updateCartItem")
    public String updateCartItem(@ModelAttribute("id") Long cartItemId, @ModelAttribute("qty") int qty){
        System.out.println("reconoce el id: " + cartItemId + " y la qty: " + qty);
        CartItem cartItem = cartItemServiceObj.findById(cartItemId);
        cartItem.setQty(qty);
        cartItemServiceObj.updateCartItem(cartItem);

        return "redirect:/shoppingCart/cart";
    }


    @GetMapping("/removeItem")
    public String removeItem(@RequestParam("id") Long id){
        System.out.println("ingresa con el id: " + id);
        cartItemServiceObj.removeCartItem(cartItemServiceObj.findById(id));

        return "redirect:/shoppingCart/cart";
    }

}
