package com.books.repository;

import com.books.domain.UserShipping;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserShippingRepository extends CrudRepository<UserShipping,Long> {

}
