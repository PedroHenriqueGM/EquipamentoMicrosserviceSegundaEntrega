package com.example.Equipamento.Repository;


import com.example.Equipamento.Model.Tranca;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrancaRepository extends JpaRepository<Tranca, Integer> {

    // === R4: existe alguma tranca ocupando esta bicicleta? ===
    boolean existsByBicicletaId(Integer bicicletaId);

    List<Tranca> findByTotemId(Long totemId);
}
