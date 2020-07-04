package com.books.service;

import com.books.domain.ShippingAddress;
import com.books.domain.UserShipping;

public interface ShippingAddressService {

    ShippingAddress setByUserShipping(UserShipping userShipping, ShippingAddress shippingAddressObj);
}
