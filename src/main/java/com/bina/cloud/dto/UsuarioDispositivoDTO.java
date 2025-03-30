package com.bina.cloud.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDispositivoDTO {
    private Long id;
    private Long usuarioId;
    private Long dispositivoId;
    private String nomeDispositivo;
    private LocalDateTime dataAssociacao;
    private LocalDateTime dataRemocao;
    private boolean ativo;
}