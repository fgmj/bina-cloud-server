package com.bina.cloud.service;

import com.bina.cloud.dto.UsuarioDispositivoDTO;
import com.bina.cloud.model.Dispositivo;
import com.bina.cloud.model.Usuario;
import com.bina.cloud.model.UsuarioDispositivo;
import com.bina.cloud.repository.DispositivoRepository;
import com.bina.cloud.repository.UsuarioDispositivoRepository;
import com.bina.cloud.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioDispositivoService {

    private final UsuarioDispositivoRepository usuarioDispositivoRepository;
    private final UsuarioRepository usuarioRepository;
    private final DispositivoRepository dispositivoRepository;

    @Transactional(readOnly = true)
    public List<UsuarioDispositivoDTO> listarDispositivosDoUsuario(Long usuarioId) {
        log.info("Listando dispositivos do usuário com ID: {}", usuarioId);
        return usuarioDispositivoRepository.findByUsuarioIdAndAtivoTrue(usuarioId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UsuarioDispositivoDTO> listarUsuariosDoDispositivo(Long dispositivoId) {
        log.info("Listando usuários do dispositivo com ID: {}", dispositivoId);
        return usuarioDispositivoRepository.findByDispositivoIdAndAtivoTrue(dispositivoId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UsuarioDispositivoDTO associarDispositivo(Long usuarioId, Long dispositivoId) {
        log.info("Associando dispositivo {} ao usuário {}", dispositivoId, usuarioId);

        if (usuarioDispositivoRepository.existsByUsuarioIdAndDispositivoIdAndAtivoTrue(usuarioId, dispositivoId)) {
            throw new RuntimeException("Este dispositivo já está associado ao usuário");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Dispositivo dispositivo = dispositivoRepository.findById(dispositivoId)
                .orElseThrow(() -> new RuntimeException("Dispositivo não encontrado"));

        UsuarioDispositivo usuarioDispositivo = new UsuarioDispositivo();
        usuarioDispositivo.setUsuario(usuario);
        usuarioDispositivo.setDispositivo(dispositivo);
        usuarioDispositivo.setAtivo(true);

        UsuarioDispositivo associacaoSalva = usuarioDispositivoRepository.save(usuarioDispositivo);
        log.info("Dispositivo associado com sucesso ao usuário");

        return convertToDTO(associacaoSalva);
    }

    @Transactional
    public void removerAssociacao(Long usuarioId, Long dispositivoId) {
        log.info("Removendo associação entre usuário {} e dispositivo {}", usuarioId, dispositivoId);

        Optional<UsuarioDispositivo> associacao = usuarioDispositivoRepository
                .findByUsuarioIdAndDispositivoIdAndAtivoTrue(usuarioId, dispositivoId);

        if (associacao.isPresent()) {
            UsuarioDispositivo usuarioDispositivo = associacao.get();
            usuarioDispositivo.setAtivo(false);
            usuarioDispositivo.setDataRemocao(LocalDateTime.now());
            usuarioDispositivoRepository.save(usuarioDispositivo);
            log.info("Associação removida com sucesso");
        } else {
            throw new RuntimeException("Associação não encontrada");
        }
    }

    private UsuarioDispositivoDTO convertToDTO(UsuarioDispositivo usuarioDispositivo) {
        return new UsuarioDispositivoDTO(
                usuarioDispositivo.getId(),
                usuarioDispositivo.getUsuario().getId(),
                usuarioDispositivo.getDispositivo().getId(),
                usuarioDispositivo.getDispositivo().getNome(),
                usuarioDispositivo.getDataAssociacao(),
                usuarioDispositivo.getDataRemocao(),
                usuarioDispositivo.isAtivo());
    }
}