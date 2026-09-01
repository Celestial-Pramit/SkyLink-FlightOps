package com.skylink.app.entity;

import com.skylink.app.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "booking_status_history",
       indexes = @Index(name = "idx_bsh_booking",
                        columnList = "booking_id, changed_at DESC"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    @Column(length = 100)
    private String changedBy;

    @Column(length = 100)
    private String changedByName;

    @Column(length = 200)
    private String note;

    @CreationTimestamp
    @Column(name = "changed_at", updatable = false)
    private LocalDateTime changedAt;

    public static BookingStatusHistory of(Booking booking,
                                          BookingStatus status,
                                          String changedBy,
                                          String changedByName,
                                          String note) {
        return BookingStatusHistory.builder()
            .booking(booking)
            .status(status)
            .changedBy(changedBy)
            .changedByName(changedByName)
            .note(note)
            .build();
    }
}