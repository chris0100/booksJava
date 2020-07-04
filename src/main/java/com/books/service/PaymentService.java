package com.books.service;

import com.books.domain.Payment;
import com.books.domain.UserPayment;

public interface PaymentService {
    Payment setByUserPayment(UserPayment userPayment, Payment payment);


}
