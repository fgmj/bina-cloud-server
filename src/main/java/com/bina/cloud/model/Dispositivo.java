package com.bina.cloud.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "dispositivos")
public class Dispositivo {

    @Id
    private String id;

    @Column(nullable = false)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @Column(name = "data_cadastro", nullable = false)
    private LocalDateTime dataCadastro;

    @Column(name = "ultima_atualizacao")
    private LocalDateTime ultimaAtualizacao;

    @Column(name = "ultima_conexao")
    private LocalDateTime lastConnection;

    @Column(length = 50)
    private String versao;

    @Column(name = "tipo_dispositivo", length = 50)
    private String tipoDispositivo;

    @Column(length = 100)
    private String localizacao;

    private boolean ativo = true;

    @PrePersist
    protected void onCreate() {
        dataCadastro = LocalDateTime.now();
        ultimaAtualizacao = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        ultimaAtualizacao = LocalDateTime.now();
    }
}