package com.example.Equipamento.Service;

import com.example.Equipamento.Model.Bicicleta;
import com.example.Equipamento.Model.Totem;
import com.example.Equipamento.Model.Tranca;
import com.example.Equipamento.Repository.BicicletaRepository;
import com.example.Equipamento.Repository.TrancaRepository;
import com.example.Equipamento.Repository.TotemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List; 
import java.util.Objects; 
import java.util.stream.Collectors; 

@Service
public class BicicletaService {
    private final BicicletaRepository repository;
    private final TrancaRepository trancaRepository;
    private final TotemRepository totemRepository;
    public BicicletaService(BicicletaRepository repository, TrancaRepository trancaRepository, TotemRepository totemRepository) {
        this.repository = repository;
        this.trancaRepository = trancaRepository;
        this.totemRepository = totemRepository; 
    }

    private static final String MSG_BICICLETA_NAO_ENCONTRADA = "Bicicleta não encontrada";

    public void incluirBicicleta(Bicicleta bicicleta) {
        // R1: status inicial "nova"
        bicicleta.setStatus("nova");

        // Primeiro salva para gerar o ID
        Bicicleta salva = repository.saveAndFlush(bicicleta);

        // R5: usa o próprio ID como número gerado pelo sistema
        salva.setNumero("BIC-" + salva.getId());

        // Atualiza o registro já com número
        repository.saveAndFlush(salva);
    }

    //private final TrancaRepository trancaRepository; // injete via construtor

    public void deletarBicicleta(Integer id) {
        Bicicleta b = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MSG_BICICLETA_NAO_ENCONTRADA));

        // R4: apenas 'aposentada'
        if (b.getStatus() == null || !b.getStatus().equalsIgnoreCase("aposentada")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "R4: apenas bicicletas com status 'aposentada' podem ser excluídas");
        }

        // R4: e NÃO pode estar em nenhuma tranca
        if (trancaRepository.existsByBicicletaId(b.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "R4: bicicleta vinculada a uma tranca não pode ser excluída");
        }

        // Soft delete: marca como 'excluida'
        b.setStatus("excluida");
        repository.saveAndFlush(b);
    }

    public Bicicleta buscarPorId(Integer id){
        return repository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, MSG_BICICLETA_NAO_ENCONTRADA));
    }


    public void atualizarBicicletaPorId(Integer id, Bicicleta req) {
        Bicicleta entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MSG_BICICLETA_NAO_ENCONTRADA));

        // R3 + R5: numero é gerado pelo sistema (BIC-{id}) e NÃO pode ser alterado
        // Se veio 'numero' diferente do atual, rejeita
        if (req.getNumero() != null && !req.getNumero().equals(entity.getNumero())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "R3/R5: o número da bicicleta é gerado pelo sistema e não pode ser alterado."
            );
        }

        // R1: status inicial é 'nova' e NÃO pode ser editado via PUT
        if (req.getStatus() != null && !req.getStatus().equalsIgnoreCase(entity.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "R1: o status não pode ser editado."
            );
        }

        // Atualiza somente campos permitidos
        entity.setMarca( req.getMarca()  != null ? req.getMarca()  : entity.getMarca() );
        entity.setModelo(req.getModelo() != null ? req.getModelo() : entity.getModelo());
        entity.setAno(   req.getAno()    != null ? req.getAno()    : entity.getAno() );
        entity.setLocalizacao(req.getLocalizacao() != null ? req.getLocalizacao() : entity.getLocalizacao());

        // Numero e Status permanecem como estão
        repository.saveAndFlush(entity);
    }

    public List<Bicicleta> listarBicicletasDoTotem(Long idTotem) {
        Totem totem = totemRepository.findById(idTotem)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Totem não encontrado."));

        return totem.getTrancas().stream()
                .map(Tranca::getBicicleta) 
                .filter(Objects::nonNull)  
                .map(bicicleta -> Bicicleta.builder()
                        .id(bicicleta.getId())
                        .numero(bicicleta.getNumero())
                        .status(bicicleta.getStatus())
                        .build())
                .collect(Collectors.toList());
    }
}
