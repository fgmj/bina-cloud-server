package com.bina.cloud.repository;

import com.bina.cloud.model.UsuarioDispositivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioDispositivoRepository extends JpaRepository<UsuarioDispositivo, Long> {

    List<UsuarioDispositivo> findByUsuarioIdAndAtivoTrue(Long usuarioId);

    List<UsuarioDispositivo> findByDispositivoIdAndAtivoTrue(Long dispositivoId);

    Optional<UsuarioDispositivo> findByUsuarioIdAndDispositivoIdAndAtivoTrue(Long usuarioId, Long dispositivoId);

    Optional<UsuarioDispositivo> findByUsuarioIdAndDispositivoIdAndAtivoFalse(Long usuarioId, Long dispositivoId);

    boolean existsByUsuarioIdAndDispositivoIdAndAtivoTrue(Long usuarioId, Long dispositivoId);

    void deleteByUsuarioIdAndDispositivoId(Long usuarioId, Long dispositivoId);
}