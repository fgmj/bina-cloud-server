package com.bina.cloud.repository;

import com.bina.cloud.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {
    List<Evento> findByPhoneNumberOrderByTimestampDesc(String phoneNumber);
}