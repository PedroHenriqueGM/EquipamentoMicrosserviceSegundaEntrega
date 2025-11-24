package com.example.Equipamento.Controller;


import com.example.Equipamento.Dto.IntegrarTrancaNaRedeDTO;
import com.example.Equipamento.Model.Tranca;
import com.example.Equipamento.Repository.TrancaRepository;
import com.example.Equipamento.Service.TrancaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tranca")
@RequiredArgsConstructor
public class TrancaController {
    private final TrancaService trancaService;

    @PostMapping
    public ResponseEntity<Void> salvarTranca(@Valid @RequestBody Tranca tranca) {
        trancaService.salvarTranca(tranca);
        return ResponseEntity.ok().build();
    }

    @Autowired
    private TrancaRepository trancaRepository;

    @GetMapping
    public ResponseEntity<List<Tranca>> listarTrancas() {
        List<Tranca> trancas = trancaRepository.findAll();
        return ResponseEntity.ok(trancas);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletarTranca(@PathVariable Integer id) {
        trancaService.deletarTranca(id);
        return ResponseEntity.ok("Tranca excluída com sucesso.");
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> atualizarTrancaPorId(
            @PathVariable Integer id,
            @RequestBody Tranca tranca) {
        trancaService.atualizarTrancaPorId(id, tranca);
        return ResponseEntity.ok().build();
    }

    // TrancaController.java (acrescente)
    @PutMapping("/{id}/trancar")
    public ResponseEntity<Void> trancar(
            @PathVariable Integer id,
            @RequestParam(value = "bicicletaNumero", required = false) String bicicletaId) {
        trancaService.trancar(id, bicicletaId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/destrancar")
    public ResponseEntity<Void> destrancar(@PathVariable Integer id) {
        trancaService.destrancar(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/integrarNaRede")
    public ResponseEntity<String> integrarTranca(@RequestBody IntegrarTrancaNaRedeDTO dto) {
        trancaService.incluirTrancaNaRede(dto);
        return ResponseEntity.ok("Tranca integrada com sucesso na rede de totens.");
    }



}
