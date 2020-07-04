package com.books.service;

import com.books.domain.CartItem;
import com.books.domain.ShoppingCart;
import com.books.repository.ShoppingCartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService{

    @Autowired
    CartItemService cartItemServiceObj;

    @Autowired
    ShoppingCartRepository shoppingCartRepositoryObj;

    @Override
    public ShoppingCart updateShoppingCart(ShoppingCart shoppingCart) {
        BigDecimal cartTotal = new BigDecimal(0);
        List<CartItem> cartItemList = cartItemServiceObj.findByShoppingCart(shoppingCart);

        for (CartItem cartItem : cartItemList){
            if (cartItem.getBook().getInStockNumber() > 0){
                cartItemServiceObj.updateCartItem(cartItem);
                cartTotal = cartTotal.add(cartItem.getSubtotal());
            }
        }
        shoppingCart.setGrandTotal(cartTotal);
        shoppingCartRepositoryObj.save(shoppingCart);

        return shoppingCart;
    }



    @Override
    public void clearShoppingCart(ShoppingCart shoppingCart) {
        List<CartItem> cartItemList = cartItemServiceObj.findByShoppingCart(shoppingCart);

        for (CartItem cartItem : cartItemList){
            cartItem.setShoppingCart(null);
            cartItemServiceObj.save(cartItem);
        }

        shoppingCart.setGrandTotal(new BigDecimal(0));
        shoppingCartRepositoryObj.save(shoppingCart);

    }
}
