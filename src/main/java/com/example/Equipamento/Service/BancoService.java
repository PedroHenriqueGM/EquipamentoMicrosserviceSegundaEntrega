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
        totem.setLocalizacao("Rio de Janeiro");
        totem.setDescricao("Totem Central");
        Totem savedTotem = totemRepository.save(totem); // ID gerado automaticamente

        // Bicicletas
        List<Bicicleta> bicicletas = bicicletaRepository.saveAll(List.of(
                bicicleta(StatusBicicleta.DISPONIVEL),
                bicicleta(StatusBicicleta.REPARO_SOLICITADO),
                bicicleta(StatusBicicleta.EM_USO),
                bicicleta(StatusBicicleta.EM_REPARO),
                bicicleta(StatusBicicleta.EM_USO)
        ));

        // Trancas
        trancaRepository.saveAll(List.of(
                tranca(StatusTranca.OCUPADA, bicicletas.get(0), savedTotem),
                tranca(StatusTranca.DISPONIVEL, null, savedTotem),
                tranca(StatusTranca.OCUPADA, bicicletas.get(1), savedTotem),
                tranca(StatusTranca.OCUPADA, bicicletas.get(4), savedTotem),
                tranca(StatusTranca.EM_REPARO, null, null),
                tranca(StatusTranca.REPARO_SOLICITADO, null, savedTotem)
        ));
    }

    private Bicicleta bicicleta(StatusBicicleta status) {
        Bicicleta b = new Bicicleta();
        b.setMarca("Caloi");
        b.setModelo("Caloi");
        b.setAno("2020");
        b.setNumero(12345);
        b.setStatus(status);
        return b;
    }

    private Tranca tranca(StatusTranca status, Bicicleta bicicleta, Totem totem) {
        Tranca t = new Tranca();
        t.setLocalizacao("Rio de Janeiro");
        t.setNumero(12345);
        t.setAno("2020");
        t.setModelo("Caloi");
        t.setStatus(status);
        t.setBicicleta(bicicleta);
        t.setTotem(totem);
        return t;
    }

}
