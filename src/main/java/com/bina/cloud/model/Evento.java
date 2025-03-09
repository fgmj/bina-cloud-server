package com.bina.cloud.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.SequenceGenerator;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "eventos")
@Data
public class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "evento_seq")
    @SequenceGenerator(name = "evento_seq", sequenceName = "evento_sequence", allocationSize = 1)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;
    
    @Column(length = 255)
    private String description;
    
    private LocalDateTime timestamp;
    
    @Column(length = 255)
    private String deviceId;
    
    @Column(length = 255)
    private String eventType;
    
    @Column(length = 255)
    private String additionalData;
} 