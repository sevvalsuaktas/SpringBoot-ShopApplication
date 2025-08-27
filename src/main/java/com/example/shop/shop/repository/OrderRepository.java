package com.example.shop.shop.repository;

import com.example.shop.shop.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(Long customerId); // customer id den siparişi bulma
    @Query("select distinct o from Order o left join fetch o.items")
    List<Order> findAllWithItems();
}

