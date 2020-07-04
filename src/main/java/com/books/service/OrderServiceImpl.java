package com.books.service;

import com.books.domain.*;
import com.books.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepositoryObj;

    @Autowired
    private CartItemService cartItemServiceObj;

    @Override
    public synchronized Order createOrder(ShoppingCart shoppingCart, ShippingAddress shippingAddress,
                                          BillingAddress billingAddress, Payment payment,
                                          String shippingMethod, User user, Date shippingDate) {
        Order order = new Order();
        order.setBillingAddress(billingAddress);
        order.setOrderStatus("created");
        order.setPayment(payment);
        order.setShippingAddress(shippingAddress);
        order.setShippingMethod(shippingMethod);
        order.setShippingDate(shippingDate);

        List<CartItem> cartItemList = cartItemServiceObj.findByShoppingCart(shoppingCart);

        for (CartItem cartItem : cartItemList){
            Book book = cartItem.getBook();
            cartItem.setOrder(order);
            book.setInStockNumber(book.getInStockNumber() - cartItem.getQty());
        }

        order.setCartItemList(cartItemList);
        order.setOrderDate(Calendar.getInstance().getTime());
        order.setOrderTotal(shoppingCart.getGrandTotal());
        shippingAddress.setOrder(order);
        billingAddress.setOrder(order);
        payment.setOrder(order);
        order.setUser(user);
        order = orderRepositoryObj.save(order);

        return order;
    }

    @Override
    public Order findOne(Long id) {
        return orderRepositoryObj.findById(id).get();
    }
}
