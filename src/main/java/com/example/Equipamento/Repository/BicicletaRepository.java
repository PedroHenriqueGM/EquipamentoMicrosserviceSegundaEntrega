package com.example.Equipamento.Repository;

import com.example.Equipamento.Model.Bicicleta;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BicicletaRepository extends JpaRepository<Bicicleta, Integer> {

    Optional<Bicicleta> findByNumero(String numero);

    @Transactional
    void deleteByNumero(String numero);
}
