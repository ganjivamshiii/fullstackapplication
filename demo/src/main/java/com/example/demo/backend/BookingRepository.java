package com.example.demo.backend;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    List<Booking> findByExclusiveId(String exclusiveId);
    
    List<Booking> findByCustomerEmail(String customerEmail);
    
    List<Booking> findByBookingStatus(String bookingStatus);
    
    List<Booking> findByExclusiveIdAndBookingStatus(String exclusiveId, String bookingStatus);
    
    List<Booking> findByLocation(String location);
    
    List<Booking> findByBookingType(String bookingType);
}