package com.books.service;

import com.books.domain.*;

import java.util.Date;

public interface OrderService {

    Order createOrder(ShoppingCart shoppingCart, ShippingAddress shippingAddress,
                      BillingAddress billingAddress, Payment payment, String shippingMethod, User user, Date shippingDate);

    Order findOne(Long id);
}
