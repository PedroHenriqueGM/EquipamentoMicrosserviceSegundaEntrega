package com.example.Equipamento.Service;

import com.example.Equipamento.Repository.TotemRepository;
import com.example.Equipamento.Repository.TrancaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.Equipamento.Repository.BicicletaRepository;

@Service
public class BancoService {

    @Autowired
    private BicicletaRepository bicicletaRepository;

    @Autowired
    private TrancaRepository trancaRepository;

    @Autowired
    private TotemRepository totemRepository;

    // Método que limpa os dados dos repositórios
    public void restaurarBanco() {
        bicicletaRepository.deleteAll(); // Limpa a tabela de bicicletas
        trancaRepository.deleteAll(); // Limpa a tabela de trancas
        totemRepository.deleteAll(); // Limpa a tabela de totems
    }
}
