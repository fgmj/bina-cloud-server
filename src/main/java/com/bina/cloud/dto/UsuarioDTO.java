package com.bina.cloud.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {

    private Long id;

    private String nome;

    private String email;

    private String password;

    private LocalDateTime dataCriacao;

    private LocalDateTime ultimoAcesso;

    private boolean ativo;
}