package com.bina.cloud.repository;

import com.bina.cloud.model.Dispositivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DispositivoRepository extends JpaRepository<Dispositivo, Long> {
    List<Dispositivo> findByAtivoTrue();

    List<Dispositivo> findByAtivoTrueOrderByUltimaConexaoDesc();

    boolean existsById(Long id);

    List<Dispositivo> findByUltimaConexaoAfter(LocalDateTime dateTime);

    Optional<Dispositivo> findByIdentificador(String identificador);

    boolean existsByIdentificador(String identificador);
}