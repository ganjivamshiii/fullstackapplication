package com.example.demo.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExclusiveRepository extends JpaRepository<Exclusive, String> {
    
    List<Exclusive> findByIsActiveTrue();
    
    List<Exclusive> findByBookingTypeAndIsActiveTrue(String bookingType);
    
    List<Exclusive> findByLocationContainingAndIsActiveTrue(String location);
    
    List<Exclusive> findByPriceBetweenAndIsActiveTrue(java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice);
}