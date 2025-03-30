package com.bina.cloud.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;

    @Column(name = "ultimo_acesso")
    private LocalDateTime ultimoAcesso;

    @Column(nullable = false)
    private boolean ativo = true;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<UsuarioDispositivo> dispositivos = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
    }
}