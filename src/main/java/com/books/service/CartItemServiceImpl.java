package com.books.service;

import com.books.domain.*;
import com.books.repository.BookToCartItemRepository;
import com.books.repository.CartItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartItemServiceImpl implements CartItemService {

    @Autowired
    CartItemRepository cartItemRepositoryObj;

    @Autowired
    BookToCartItemRepository bookToCartItemRepositoryObj;

    @Override
    public List<CartItem> findByShoppingCart(ShoppingCart shoppingCart) {
        return cartItemRepositoryObj.findByShoppingCart(shoppingCart);
    }

    @Override
    public CartItem updateCartItem(CartItem cartItem) {
        BigDecimal bigDecimal = new BigDecimal(cartItem.getBook().getOurPrice())
                .multiply(new BigDecimal(cartItem.getQty()));
        bigDecimal = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
        cartItem.setSubtotal(bigDecimal);

        cartItemRepositoryObj.save(cartItem);

        return cartItem;
    }

    @Override
    public CartItem addBookToCartItem(Book book, User user, int qty) {
        List<CartItem> cartItemList = findByShoppingCart(user.getShoppingCart());

        for (CartItem cartItem : cartItemList){
            if(book.getId().equals(cartItem.getBook().getId())){
                cartItem.setQty(cartItem.getQty() + qty);
                cartItem.setSubtotal(new BigDecimal(book.getOurPrice())
                        .multiply(new BigDecimal(qty)));
                cartItemRepositoryObj.save(cartItem);
                return cartItem;
            }
        }
        
        CartItem cartItemNew = new CartItem();
        cartItemNew.setShoppingCart(user.getShoppingCart());
        cartItemNew.setBook(book);
        cartItemNew.setQty(qty);
        cartItemNew.setSubtotal(new BigDecimal(book.getOurPrice())
                .multiply(new BigDecimal(qty)));
        cartItemNew = cartItemRepositoryObj.save(cartItemNew);

        BookToCartItem bookToCartItem = new BookToCartItem();
        bookToCartItem.setBook(book);
        bookToCartItem.setCartItem(cartItemNew);
        bookToCartItemRepositoryObj.save(bookToCartItem);
        return cartItemNew;
    }


    @Override
    public CartItem findById(Long id) {
        return cartItemRepositoryObj.findById(id).get();
    }


    @Override
    public void removeCartItem(CartItem cartItem) {
        bookToCartItemRepositoryObj.deleteByCartItem(cartItem);
        cartItemRepositoryObj.delete(cartItem);
    }

    @Override
    public CartItem save(CartItem cartItem) {
        return cartItemRepositoryObj.save(cartItem);
    }

    @Override
    public List<CartItem> findByOrder(Order order) {
        return cartItemRepositoryObj.findByOrder(order);
    }
}
