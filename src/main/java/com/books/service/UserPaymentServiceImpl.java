package com.books.service;

import com.books.domain.UserPayment;
import com.books.repository.UserPaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class UserPaymentServiceImpl implements UserPaymentService {

    @Autowired
    UserPaymentRepository userPaymentRepositoryObj;

    @Override
    public UserPayment findById(Long id) {
        return userPaymentRepositoryObj.findById(id).get();
    }


    @Override
    public void removeById(Long id) {
        userPaymentRepositoryObj.deleteById(id);
    }
}
