package com.books.service;

import com.books.domain.UserShipping;
import com.books.repository.UserShippingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserShippingServiceImpl implements UserShippingService {

    @Autowired
    UserShippingRepository userShippingRepositoryObj;


    @Override
    public UserShipping findById(Long id) {
        return userShippingRepositoryObj.findById(id).get();
    }


    @Override
    public void removeById(Long id) {
        userShippingRepositoryObj.deleteById(id);
    }
}
