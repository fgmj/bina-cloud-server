package com.bina.cloud.repository;

import com.bina.cloud.model.Dispositivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DispositivoRepository extends JpaRepository<Dispositivo, String> {
    List<Dispositivo> findByAtivoTrue();

    List<Dispositivo> findByAtivoTrueOrderByLastConnectionDesc();

    boolean existsById(String id);

    List<Dispositivo> findByLastConnectionAfter(LocalDateTime dateTime);
}