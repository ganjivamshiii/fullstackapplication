package com.example.demo.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {
    
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ExclusiveService exclusiveService;

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody Map<String, Object> bookingData) {
        try {
            // Create booking object
            Booking booking = new Booking();
            
            // Set basic fields
            booking.setExclusiveId((String) bookingData.get("exclusiveId"));
            booking.setCustomerName((String) bookingData.get("customerName"));
            booking.setCustomerEmail((String) bookingData.get("customerEmail"));
            booking.setBookingType((String) bookingData.get("bookingType"));
            booking.setLocation((String) bookingData.get("location"));
            
            // Handle numeric fields safely
            if (bookingData.get("numberOfGuests") != null) {
                booking.setNumberOfGuests(((Number) bookingData.get("numberOfGuests")).intValue());
            } else {
                booking.setNumberOfGuests(1);
            }
            
            if (bookingData.get("totalAmount") != null) {
                booking.setTotalAmount(new BigDecimal(bookingData.get("totalAmount").toString()));
            }
            
            // Handle booking date
            if (bookingData.get("bookingDate") != null) {
                booking.setBookingDate((String) bookingData.get("bookingDate"));
            } else {
                booking.setBookingDate(LocalDateTime.now());
            }
            
            // Set default values
            if (bookingData.get("bookingStatus") != null) {
                booking.setBookingStatus((String) bookingData.get("bookingStatus"));
            } else {
                booking.setBookingStatus("PENDING");
            }
            
            // Set timestamps
            booking.setCreatedAt(LocalDateTime.now());
            booking.setUpdatedAt(LocalDateTime.now());

            // Validate required fields
            if (booking.getExclusiveId() == null || booking.getExclusiveId().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Exclusive ID is required"));
            }
            
            if (booking.getCustomerName() == null || booking.getCustomerName().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Customer name is required"));
            }
            
            if (booking.getCustomerEmail() == null || booking.getCustomerEmail().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Customer email is required"));
            }

            // Save the booking
            Booking savedBooking = bookingRepository.save(booking);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(savedBooking);
            
        } catch (Exception e) {
            System.err.println("Error creating booking: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to create booking: " + e.getMessage()));
        }
    }

    @GetMapping("/exclusive/{exclusiveId}")
    public ResponseEntity<List<Booking>> getBookingsByExclusive(@PathVariable String exclusiveId) {
        List<Booking> bookings = exclusiveService.getBookingsByExclusive(exclusiveId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/customer/{customerEmail}")
    public ResponseEntity<List<Booking>> getBookingsByCustomer(@PathVariable String customerEmail) {
        List<Booking> bookings = exclusiveService.getBookingsByCustomerEmail(customerEmail);
        return ResponseEntity.ok(bookings);
    }

    @PutMapping("/{bookingId}/status")
    public ResponseEntity<?> updateBookingStatus(@PathVariable Long bookingId, @RequestBody Map<String, String> statusUpdate) {
        String status = statusUpdate.get("status");
        if (status == null || status.trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Status is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    
        Booking updatedBooking = exclusiveService.updateBookingStatus(bookingId, status);
        if (updatedBooking != null) {
            return ResponseEntity.ok(updatedBooking);
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Booking not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<?> getBookingById(@PathVariable Long bookingId) {
        return bookingRepository.findById(bookingId)
            .map(booking -> ResponseEntity.ok().body(booking))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/check-availability/{exclusiveId}")
    public ResponseEntity<Map<String, Object>> checkAvailability(@PathVariable String exclusiveId, @RequestParam int guests) {
        boolean available = exclusiveService.checkAvailability(exclusiveId, guests);
        Exclusive exclusive = exclusiveService.getExclusive(exclusiveId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("available", available);
        response.put("availableSlots", exclusive != null ? exclusive.getAvailableSlots() : 0);
        response.put("requestedGuests", guests);
        
        return ResponseEntity.ok(response);
    }
}