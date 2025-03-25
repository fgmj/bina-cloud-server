package com.bina.cloud.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "eventos")
public class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private String phoneNumber;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String deviceId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EventType eventType;

    private String description;

    @Column(columnDefinition = "TEXT")
    private String additionalData;
}