package com.books.service;

import com.books.domain.BillingAddress;
import com.books.domain.UserBilling;

public interface BillingAddressService {

    BillingAddress setByUserBilling(UserBilling userBilling, BillingAddress billingAddress);
}
