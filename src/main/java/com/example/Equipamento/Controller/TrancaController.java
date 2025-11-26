package com.example.Equipamento.Controller;

import com.example.Equipamento.Dto.IntegrarTrancaNaRedeDTO;
import com.example.Equipamento.Dto.RetirarTrancaDTO;
import com.example.Equipamento.Model.Bicicleta;
import com.example.Equipamento.Model.Tranca;
import com.example.Equipamento.Repository.TrancaRepository;
import com.example.Equipamento.Service.TrancaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tranca")
@RequiredArgsConstructor
public class TrancaController {

    private final TrancaService trancaService;
    private final TrancaRepository trancaRepository;

    // POST /tranca – retorna Tranca
    @PostMapping
    public ResponseEntity<Tranca> salvarTranca(@Valid @RequestBody Tranca tranca) {
        Tranca salva = trancaService.salvarTranca(tranca);
        return ResponseEntity.ok(salva);
    }

    // GET /tranca – lista de trancas
    @GetMapping
    public ResponseEntity<List<Tranca>> listarTrancas() {
        List<Tranca> trancas = trancaRepository.findAll();
        return ResponseEntity.ok(trancas);
    }

    // GET /tranca/{idTranca}
    @GetMapping("/{idTranca}")
    public ResponseEntity<Tranca> buscarPorId(@PathVariable Integer idTranca) {
        Tranca tranca = trancaService.buscarPorId(idTranca);
        return ResponseEntity.ok(tranca);
    }

    // DELETE /tranca/{idTranca} – 200 sem corpo
    @DeleteMapping("/{idTranca}")
    public ResponseEntity<Void> deletarTranca(@PathVariable Integer idTranca) {
        trancaService.deletarTranca(idTranca);
        return ResponseEntity.ok().build();
    }

    // PUT /tranca/{idTranca} – 200 + Tranca atualizada
    @PutMapping("/{idTranca}")
    public ResponseEntity<Tranca> atualizarTrancaPorId(
            @PathVariable Integer idTranca,
            @RequestBody Tranca tranca) {

        Tranca atualizada = trancaService.atualizarTrancaPorId(idTranca, tranca);
        return ResponseEntity.ok(atualizada);
    }

    // GET /tranca/{idTranca}/bicicleta – retorna Bicicleta associada
    @GetMapping("/{idTranca}/bicicleta")
    public ResponseEntity<Bicicleta> buscarBicicletaDaTranca(@PathVariable Integer idTranca) {
        Bicicleta bicicleta = trancaService.buscarBicicletaDaTranca(idTranca);
        return ResponseEntity.ok(bicicleta);
    }

    // POST /tranca/{idTranca}/trancar – body opcional { "bicicleta": 1 }
    @PostMapping("/{idTranca}/trancar")
    public ResponseEntity<Tranca> trancar(
            @PathVariable Integer idTranca,
            @RequestBody(required = false) Map<String, Integer> body) {

        Integer idBicicleta = body != null ? body.get("bicicleta") : null;
        Tranca tranca = trancaService.trancar(idTranca, idBicicleta);
        return ResponseEntity.ok(tranca);
    }

    // POST /tranca/{idTranca}/destrancar – body opcional { "bicicleta": 1 }
    @PostMapping("/{idTranca}/destrancar")
    public ResponseEntity<Tranca> destrancar(
            @PathVariable Integer idTranca,
            @RequestBody(required = false) Map<String, Integer> body) {

        Integer idBicicleta = body != null ? body.get("bicicleta") : null;
        Tranca tranca = trancaService.destrancar(idTranca, idBicicleta);
        return ResponseEntity.ok(tranca);
    }

    // POST /tranca/integrarNaRede – 200 sem corpo
    @PostMapping("/integrarNaRede")
    public ResponseEntity<Void> integrarTranca(@RequestBody IntegrarTrancaNaRedeDTO dto) {
        trancaService.incluirTrancaNaRede(dto);
        return ResponseEntity.ok().build();
    }

    // POST /tranca/retirarDaRede – 200 sem corpo
    @PostMapping("/retirarDaRede")
    public ResponseEntity<Void> retirarTranca(@RequestBody RetirarTrancaDTO dto) {
        trancaService.retirarTranca(dto);
        return ResponseEntity.ok().build();
    }

    // POST /tranca/{idTranca}/status/{acao} – 200 + Tranca
    @PostMapping("/{idTranca}/status/{acao}")
    public ResponseEntity<Tranca> alterarStatusTranca(
            @PathVariable Integer idTranca,
            @PathVariable String acao) {

        Tranca atualizada = trancaService.alterarStatus(idTranca, acao);
        return ResponseEntity.ok(atualizada);
    }
}
