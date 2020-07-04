package com.books.repository;

import com.books.domain.BookToCartItem;
import com.books.domain.CartItem;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface BookToCartItemRepository extends CrudRepository<BookToCartItem,Long> {

    void deleteByCartItem(CartItem cartItem);

}
