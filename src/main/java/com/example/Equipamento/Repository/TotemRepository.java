package com.example.Equipamento.Repository;

import com.example.Equipamento.Model.Totem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TotemRepository extends JpaRepository<Totem, Long> {
}
