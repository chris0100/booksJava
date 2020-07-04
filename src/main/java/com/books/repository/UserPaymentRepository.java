package com.books.repository;

import com.books.domain.UserPayment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserPaymentRepository extends CrudRepository<UserPayment,Long> {

}
