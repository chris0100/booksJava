package com.books.service;

import com.books.domain.UserPayment;

public interface UserPaymentService {

    UserPayment findById(Long id);

    void removeById(Long id);

}
