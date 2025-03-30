package com.bina.cloud.service;

import com.bina.cloud.model.Dispositivo;
import com.bina.cloud.repository.DispositivoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DispositivoService {

    private final DispositivoRepository dispositivoRepository;

    @Transactional(readOnly = true)
    public List<Dispositivo> listarTodos() {
        log.info("Listando todos os dispositivos");
        List<Dispositivo> dispositivos = dispositivoRepository.findAll();
        // Force initialization of the usuarios collection
        dispositivos.forEach(d -> d.getUsuarios().size());
        log.info("Total de dispositivos encontrados: {}", dispositivos.size());
        return dispositivos;
    }

    @Transactional(readOnly = true)
    public List<Dispositivo> listarAtivos() {
        log.info("Listando dispositivos ativos");
        List<Dispositivo> dispositivos = dispositivoRepository.findByAtivoTrue();
        // Force initialization of the usuarios collection
        dispositivos.forEach(d -> d.getUsuarios().size());
        return dispositivos;
    }

    @Transactional(readOnly = true)
    public Optional<Dispositivo> buscarPorId(Long id) {
        log.info("Buscando dispositivo por ID: {}", id);
        Optional<Dispositivo> dispositivo = dispositivoRepository.findById(id);
        dispositivo.ifPresent(d -> d.getUsuarios().size()); // Force initialization
        log.info("Dispositivo encontrado: {}", dispositivo.isPresent());
        return dispositivo;
    }

    @Transactional(readOnly = true)
    public Optional<Dispositivo> buscarPorIdentificador(String identificador) {
        log.info("Buscando dispositivo por identificador: {}", identificador);
        Optional<Dispositivo> dispositivo = dispositivoRepository.findByIdentificador(identificador);
        dispositivo.ifPresent(d -> d.getUsuarios().size()); // Force initialization
        log.info("Dispositivo encontrado: {}", dispositivo.isPresent());
        return dispositivo;
    }

    @Transactional
    public Dispositivo salvar(Dispositivo dispositivo) {
        log.info("Salvando dispositivo: {}", dispositivo.getNome());
        return dispositivoRepository.save(dispositivo);
    }

    @Transactional
    public void excluir(Long id) {
        log.info("Excluindo dispositivo com ID: {}", id);
        dispositivoRepository.deleteById(id);
    }

    @Transactional
    public Dispositivo desativar(Long id) {
        log.info("Desativando dispositivo com ID: {}", id);
        return dispositivoRepository.findById(id)
                .map(dispositivo -> {
                    dispositivo.setAtivo(false);
                    return dispositivoRepository.save(dispositivo);
                })
                .orElseThrow(() -> new RuntimeException("Dispositivo não encontrado"));
    }

    @Transactional
    public Dispositivo registrarConexao(String identificador) {
        log.info("Registrando conexão para dispositivo: {}", identificador);
        return buscarPorIdentificador(identificador)
                .map(dispositivo -> {
                    dispositivo.setUltimaConexao(LocalDateTime.now());
                    return dispositivoRepository.save(dispositivo);
                })
                .orElseGet(() -> {
                    // Auto-cadastro do dispositivo
                    Dispositivo novoDispositivo = new Dispositivo();
                    novoDispositivo.setNome("Dispositivo " + identificador);
                    novoDispositivo.setIdentificador(identificador);
                    novoDispositivo.setUltimaConexao(LocalDateTime.now());
                    return dispositivoRepository.save(novoDispositivo);
                });
    }

    @Transactional
    public Dispositivo atualizar(Long id, Dispositivo dispositivoAtualizado) {
        log.info("Atualizando dispositivo com ID: {}", id);
        return dispositivoRepository.findById(id)
                .map(dispositivo -> {
                    dispositivo.setNome(dispositivoAtualizado.getNome());
                    dispositivo.setIdentificador(dispositivoAtualizado.getIdentificador());
                    dispositivo.setAtivo(dispositivoAtualizado.isAtivo());
                    return dispositivoRepository.save(dispositivo);
                })
                .orElseThrow(() -> new RuntimeException("Dispositivo não encontrado"));
    }

    @Transactional
    public void atualizarUltimaConexao(String identificador) {
        log.info("Atualizando última conexão para dispositivo: {}", identificador);
        Dispositivo dispositivo = buscarPorIdentificador(identificador)
                .orElseGet(() -> {
                    Dispositivo novo = new Dispositivo();
                    novo.setNome("Dispositivo " + identificador);
                    novo.setIdentificador(identificador);
                    return novo;
                });

        dispositivo.setUltimaConexao(LocalDateTime.now());
        dispositivoRepository.save(dispositivo);
        log.info("Última conexão atualizada para o dispositivo {}: {}", identificador, dispositivo.getUltimaConexao());
    }

    public boolean existePorId(Long id) {
        return dispositivoRepository.existsById(id);
    }
}