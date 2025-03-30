package com.bina.cloud.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuario_dispositivo")
public class UsuarioDispositivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "dispositivo_id", nullable = false)
    private Dispositivo dispositivo;

    @Column(name = "data_associacao")
    private LocalDateTime dataAssociacao;

    @Column(name = "data_remocao")
    private LocalDateTime dataRemocao;

    @Column(name = "ativo")
    private boolean ativo = true;

    @PrePersist
    protected void onCreate() {
        dataAssociacao = LocalDateTime.now();
    }
}