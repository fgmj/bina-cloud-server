package com.bina.cloud.service;

import com.bina.cloud.dto.UsuarioDTO;
import com.bina.cloud.model.Usuario;
import com.bina.cloud.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UsuarioDTO> listarTodos() {
        log.info("Listando todos os usuários");
        return usuarioRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<UsuarioDTO> buscarPorId(Long id) {
        log.info("Buscando usuário com ID: {}", id);
        return usuarioRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Transactional
    public UsuarioDTO criar(UsuarioDTO usuarioDTO) {
        log.info("Criando novo usuário: {}", usuarioDTO.getEmail());

        if (usuarioRepository.existsByEmail(usuarioDTO.getEmail())) {
            throw new RuntimeException("Email já está em uso");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(usuarioDTO.getNome());
        usuario.setEmail(usuarioDTO.getEmail());
        usuario.setPassword(passwordEncoder.encode(usuarioDTO.getPassword()));
        usuario.setAtivo(true);
        usuario.setDataCriacao(LocalDateTime.now());

        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        log.info("Usuário criado com sucesso: {}", usuarioSalvo.getEmail());

        return convertToDTO(usuarioSalvo);
    }

    @Transactional
    public Optional<UsuarioDTO> atualizar(Long id, UsuarioDTO usuarioDTO) {
        log.info("Atualizando usuário com ID: {}", id);

        return usuarioRepository.findById(id)
                .map(usuario -> {
                    if (!usuario.getEmail().equals(usuarioDTO.getEmail()) &&
                            usuarioRepository.existsByEmail(usuarioDTO.getEmail())) {
                        throw new RuntimeException("Email já está em uso");
                    }

                    usuario.setNome(usuarioDTO.getNome());
                    usuario.setEmail(usuarioDTO.getEmail());
                    if (usuarioDTO.getPassword() != null && !usuarioDTO.getPassword().isEmpty()) {
                        usuario.setPassword(passwordEncoder.encode(usuarioDTO.getPassword()));
                    }
                    usuario.setAtivo(usuarioDTO.isAtivo());

                    Usuario usuarioAtualizado = usuarioRepository.save(usuario);
                    log.info("Usuário atualizado com sucesso: {}", usuarioAtualizado.getEmail());

                    return convertToDTO(usuarioAtualizado);
                });
    }

    @Transactional
    public void deletar(Long id) {
        log.info("Deletando usuário com ID: {}", id);
        usuarioRepository.deleteById(id);
        log.info("Usuário deletado com sucesso");
    }

    @Transactional
    public void atualizarUltimoAcesso(String email) {
        log.info("Atualizando último acesso do usuário: {}", email);
        usuarioRepository.findByEmail(email)
                .ifPresent(usuario -> {
                    usuario.setUltimoAcesso(LocalDateTime.now());
                    usuarioRepository.save(usuario);
                    log.info("Último acesso atualizado com sucesso");
                });
    }

    private UsuarioDTO convertToDTO(Usuario usuario) {
        return new UsuarioDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPassword(),
                usuario.getDataCriacao(),
                usuario.getUltimoAcesso(),
                usuario.isAtivo());
    }
}