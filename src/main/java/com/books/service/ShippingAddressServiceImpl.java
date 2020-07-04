package com.books.service;

import com.books.domain.ShippingAddress;
import com.books.domain.UserShipping;
import org.springframework.stereotype.Service;

@Service
public class ShippingAddressServiceImpl implements ShippingAddressService {

    @Override
    public ShippingAddress setByUserShipping(UserShipping userShipping, ShippingAddress shippingAddressObj) {
        shippingAddressObj.setShippingAddressName(userShipping.getUserShippingName());
        shippingAddressObj.setShippingAddressStreet1(userShipping.getUserShippingStreet1());
        shippingAddressObj.setShippingAddressStreet2(userShipping.getUserShippingStreet2());
        shippingAddressObj.setShippingAddressCity(userShipping.getUserShippingCity());
        shippingAddressObj.setShippingAddressState(userShipping.getUserShippingState());
        shippingAddressObj.setShippingAddressCountry(userShipping.getUserShippingCountry());
        shippingAddressObj.setShippingAddressZipcode(userShipping.getUserShippingZipcode());
        return shippingAddressObj;
    }
}
