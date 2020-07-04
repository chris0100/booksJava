package com.books.service;

import com.books.domain.UserShipping;

public interface UserShippingService {

    UserShipping findById(Long id);

    void removeById(Long id);
}
