package com.soumadeep.SpringEcommerce.Repo;

import com.soumadeep.SpringEcommerce.Entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepo extends JpaRepository<Order,Long> {
}
