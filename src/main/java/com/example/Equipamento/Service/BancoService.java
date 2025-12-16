package com.example.Equipamento.Service;

import com.example.Equipamento.Model.Bicicleta;
import com.example.Equipamento.Model.Totem;
import com.example.Equipamento.Model.Tranca;
import com.example.Equipamento.Model.enums.StatusBicicleta;
import com.example.Equipamento.Model.enums.StatusTranca;
import com.example.Equipamento.Repository.BicicletaRepository;
import com.example.Equipamento.Repository.TotemRepository;
import com.example.Equipamento.Repository.TrancaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BancoService {

    private final BicicletaRepository bicicletaRepository;
    private final TrancaRepository trancaRepository;
    private final TotemRepository totemRepository;

    public BancoService(
            BicicletaRepository bicicletaRepository,
            TrancaRepository trancaRepository,
            TotemRepository totemRepository) {
        this.bicicletaRepository = bicicletaRepository;
        this.trancaRepository = trancaRepository;
        this.totemRepository = totemRepository;
    }

    @Transactional
    public void restaurarBanco() {

        trancaRepository.deleteAll();
        bicicletaRepository.deleteAll();
        totemRepository.deleteAll();

        // Totem
        Totem totem = new Totem();
        totem.setId(1L);
        totem.setLocalizacao("Rio de Janeiro");
        totemRepository.save(totem);

        // Bicicletas
        bicicletaRepository.saveAll(List.of(
                bicicleta(1, StatusBicicleta.DISPONIVEL),
                bicicleta(2, StatusBicicleta.REPARO_SOLICITADO),
                bicicleta(3, StatusBicicleta.EM_USO),
                bicicleta(4, StatusBicicleta.EM_REPARO),
                bicicleta(5, StatusBicicleta.EM_USO)
        ));

        // Trancas
        trancaRepository.saveAll(List.of(
                tranca(1, StatusTranca.OCUPADA, 1, totem),
                tranca(2, StatusTranca.DISPONIVEL, null, totem),
                tranca(3, StatusTranca.OCUPADA, 2, totem),
                tranca(4, StatusTranca.OCUPADA, 5, totem),
                tranca(5, StatusTranca.EM_REPARO, null, null),
                tranca(6, StatusTranca.REPARO_SOLICITADO, null, totem)
        ));
    }

    private Bicicleta bicicleta(int id, StatusBicicleta status) {
        Bicicleta b = new Bicicleta();
        b.setId(id);
        b.setMarca("Caloi");
        b.setModelo("Caloi");
        b.setAno("2020");
        b.setNumero(12345);
        b.setStatus(status);
        return b;
    }

    private Tranca tranca(int id, StatusTranca status, Integer bicicletaId, Totem totem) {
        Tranca t = new Tranca();
        t.setId(id);
        t.setLocalizacao("Rio de Janeiro");
        t.setNumero(12345);
        t.setAno("2020");
        t.setModelo("Caloi");
        t.setStatus(status);

        if (bicicletaId != null) {
            t.setBicicleta(bicicletaRepository.findById(bicicletaId).orElse(null));
        }

        t.setTotem(totem);

        return t;
    }
}
