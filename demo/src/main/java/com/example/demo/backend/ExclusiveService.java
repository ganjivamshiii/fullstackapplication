package com.example.demo.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Service
public class ExclusiveService {

    private final ExclusiveRepository exclusiveRepository;
    private final BookingRepository bookingRepository;

    @Autowired
    public ExclusiveService(ExclusiveRepository exclusiveRepository, BookingRepository bookingRepository) {
        this.exclusiveRepository = exclusiveRepository;
        this.bookingRepository = bookingRepository;
    }

    public List<Exclusive> listExclusives() {
        return exclusiveRepository.findByIsActiveTrue();
    }

    public List<Exclusive> listExclusivesByType(String bookingType) {
        return exclusiveRepository.findByBookingTypeAndIsActiveTrue(bookingType);
    }

    public Exclusive getExclusive(String id) {
        return exclusiveRepository.findById(id).orElse(null);
    }

    public Exclusive createExclusive(Exclusive exclusive) {
        exclusive.setId(generateId());
        exclusive.setCreatedAt(LocalDateTime.now());
        exclusive.setUpdatedAt(LocalDateTime.now());
        return exclusiveRepository.save(exclusive);
    }

    public Exclusive updateExclusive(String id, Exclusive exclusive) {
        if (!exclusiveRepository.existsById(id)) {
            return null;
        }
        exclusive.setId(id);
        exclusive.updateTimestamp();
        return exclusiveRepository.save(exclusive);
    }

    public void deleteExclusive(String id) {
        // Soft delete by setting isActive to false
        Exclusive exclusive = exclusiveRepository.findById(id).orElse(null);
        if (exclusive != null) {
            exclusive.setIsActive(false);
            exclusive.updateTimestamp();
            exclusiveRepository.save(exclusive);
        }
    }

    // Booking related methods - Updated to handle Long IDs
    public Booking createBooking(Booking booking) {
        // Remove manual ID generation since we're using auto-increment
        return bookingRepository.save(booking);
    }

    public List<Booking> getBookingsByExclusive(String exclusiveId) {
        return bookingRepository.findByExclusiveId(exclusiveId);
    }

    public List<Booking> getBookingsByCustomerEmail(String customerEmail) {
        return bookingRepository.findByCustomerEmail(customerEmail);
    }

    // Updated to handle Long booking IDs
    public Booking updateBookingStatus(Long bookingId, String status) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking != null) {
            booking.setBookingStatus(status);
            booking.updateTimestamp();
            return bookingRepository.save(booking);
        }
        return null;
    }

    // Overloaded method to handle String bookingId (for backward compatibility)
    public Booking updateBookingStatus(String bookingId, String status) {
        try {
            Long id = Long.parseLong(bookingId);
            return updateBookingStatus(id, status);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public boolean checkAvailability(String exclusiveId, int requiredSlots) {
        Exclusive exclusive = getExclusive(exclusiveId);
        if (exclusive == null || exclusive.getAvailableSlots() == null) {
            return false;
        }
        return exclusive.getAvailableSlots() >= requiredSlots;
    }

    public Exclusive updateAvailableSlots(String exclusiveId, int bookedSlots) {
        Exclusive exclusive = getExclusive(exclusiveId);
        if (exclusive != null && exclusive.getAvailableSlots() != null) {
            exclusive.setAvailableSlots(exclusive.getAvailableSlots() - bookedSlots);
            exclusive.updateTimestamp();
            return exclusiveRepository.save(exclusive);
        }
        return null;
    }

    private String generateId() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[8];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}