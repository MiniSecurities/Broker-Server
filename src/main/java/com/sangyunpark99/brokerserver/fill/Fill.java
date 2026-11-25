package com.sangyunpark99.brokerserver.fill;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access =  AccessLevel.PROTECTED)
public class Fill {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String fillId;

    @Column(nullable = false)
    private Long orderId;

    private long quantity;

    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FillStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime fillAt;

    public Fill(String fillId,  Long orderId, long quantity, BigDecimal price, LocalDateTime fillAt) {
        this.fillId = fillId;
        this.quantity = quantity;
        this.price = price;
        this.status = FillStatus.SCHEDULED;
        this.orderId = orderId;
        this.createdAt = LocalDateTime.now();
        this.fillAt = fillAt;
    }

    public void markSent() {
        if(status == FillStatus.PENDING) {
            this.status = FillStatus.SENT;
        }
    }

    public void markFilled() {
        if(status == FillStatus.SCHEDULED) {
            this.status = FillStatus.PENDING;
        }
    }
}
