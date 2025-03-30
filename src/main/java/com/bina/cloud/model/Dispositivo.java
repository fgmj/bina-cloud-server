package com.bina.cloud.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "dispositivos")
public class Dispositivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String identificador;

    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro;

    @Column(name = "ultima_conexao")
    private LocalDateTime ultimaConexao;

    @Column(nullable = false)
    private boolean ativo = true;

    @OneToMany(mappedBy = "dispositivo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<UsuarioDispositivo> usuarios = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        dataCadastro = LocalDateTime.now();
    }
}