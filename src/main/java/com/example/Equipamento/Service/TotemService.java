package com.example.Equipamento.Service;

import com.example.Equipamento.Dto.TotemDTO;
import com.example.Equipamento.Model.Totem;
import com.example.Equipamento.Repository.TotemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;


import java.util.List;
import java.util.stream.Collectors;

@Service
public class TotemService {

    private final TotemRepository repository;

    public TotemService(TotemRepository repository) {
        this.repository = repository;
    }

    public List<TotemDTO> listarTotens() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    @Transactional
    public TotemDTO incluirTotem(NovoTotemDTO dto) {
        Totem totem = new Totem();
        totem.setLocalizacao(dto.localizacao());
        totem.setDescricao(dto.descricao());
        
        Totem totemSalvo = repository.save(totem);
        return toDTO(totemSalvo);
    }

    public TotemDTO atualizarTotem(Long idTotem, NovoTotemDTO dto) {
        Totem totem = repository.findById(idTotem)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Totem não encontrado."
                ));

        // Aqui, como os casos de uso não colocam restrição extra,
        // só garantimos que localizacao e descricao não sejam vazias.
        if (dto.localizacao() == null || dto.localizacao().isBlank()
                || dto.descricao() == null || dto.descricao().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Localização e descrição são obrigatórias para atualizar o totem."
            );
        }

        totem.setLocalizacao(dto.localizacao());
        totem.setDescricao(dto.descricao());

        Totem salvo = repository.saveAndFlush(totem);
        return toDTO(salvo);
    }

    @Transactional
    public void excluirTotem(Long id) {
        Totem totem = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Totem não encontrado."));

        // [R3] Apenas totens sem trancas podem ser excluídos
        if (totem.getTrancas() != null && !totem.getTrancas().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Totem possui trancas e não pode ser excluído.");
        }
        repository.delete(totem);
    }
    
    private TotemDTO toDTO(Totem totem) {
        return new TotemDTO(
            totem.getId(),
            totem.getLocalizacao(),
            totem.getDescricao(),
            totem.getTrancas() != null ? totem.getTrancas().size() : 0
        );
    }
}
