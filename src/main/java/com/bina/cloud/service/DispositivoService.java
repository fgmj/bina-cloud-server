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
        return dispositivoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Dispositivo> listarAtivos() {
        return dispositivoRepository.findByAtivoTrue();
    }

    @Transactional(readOnly = true)
    public Optional<Dispositivo> buscarPorId(String id) {
        log.info("Buscando dispositivo por ID: {}", id);
        Optional<Dispositivo> dispositivo = dispositivoRepository.findById(id);
        log.info("Dispositivo encontrado: {}", dispositivo.isPresent());
        return dispositivo;
    }

    @Transactional
    public Dispositivo salvar(Dispositivo dispositivo) {
        return dispositivoRepository.save(dispositivo);
    }

    @Transactional
    public void excluir(String id) {
        dispositivoRepository.deleteById(id);
    }

    @Transactional
    public Dispositivo desativar(String id) {
        return dispositivoRepository.findById(id)
                .map(dispositivo -> {
                    dispositivo.setAtivo(false);
                    return dispositivoRepository.save(dispositivo);
                })
                .orElseThrow(() -> new RuntimeException("Dispositivo não encontrado"));
    }

    @Transactional
    public Dispositivo registrarConexao(String id) {
        return dispositivoRepository.findById(id)
                .map(dispositivo -> {
                    dispositivo.setLastConnection(LocalDateTime.now());
                    return dispositivoRepository.save(dispositivo);
                })
                .orElseGet(() -> {
                    // Auto-cadastro do dispositivo
                    Dispositivo novoDispositivo = new Dispositivo();
                    novoDispositivo.setId(id);
                    novoDispositivo.setNome("Dispositivo " + id);
                    novoDispositivo.setLastConnection(LocalDateTime.now());
                    return dispositivoRepository.save(novoDispositivo);
                });
    }

    @Transactional
    public Dispositivo atualizar(String id, Dispositivo dispositivoAtualizado) {
        return dispositivoRepository.findById(id)
                .map(dispositivo -> {
                    dispositivo.setNome(dispositivoAtualizado.getNome());
                    dispositivo.setDescricao(dispositivoAtualizado.getDescricao());
                    dispositivo.setVersao(dispositivoAtualizado.getVersao());
                    dispositivo.setTipoDispositivo(dispositivoAtualizado.getTipoDispositivo());
                    dispositivo.setLocalizacao(dispositivoAtualizado.getLocalizacao());
                    dispositivo.setAtivo(dispositivoAtualizado.isAtivo());
                    return dispositivoRepository.save(dispositivo);
                })
                .orElseThrow(() -> new RuntimeException("Dispositivo não encontrado"));
    }

    @Transactional
    public void atualizarUltimaConexao(String deviceId) {
        Dispositivo dispositivo = dispositivoRepository.findById(deviceId)
                .orElseGet(() -> {
                    Dispositivo novo = new Dispositivo();
                    novo.setId(deviceId);
                    novo.setNome(deviceId);
                    novo.setTipoDispositivo("Telefone");
                    novo.setLocalizacao("Localização não especificada");
                    return novo;
                });

        dispositivo.setLastConnection(LocalDateTime.now());
        dispositivoRepository.save(dispositivo);
        log.info("Última conexão atualizada para o dispositivo {}: {}", deviceId, dispositivo.getLastConnection());
    }

    public List<Dispositivo> listarDispositivos() {
        log.info("Listando todos os dispositivos");
        List<Dispositivo> dispositivos = dispositivoRepository.findAll();
        log.info("Total de dispositivos encontrados: {}", dispositivos.size());
        return dispositivos;
    }

    public boolean existePorId(String id) {
        return dispositivoRepository.existsById(id);
    }
}