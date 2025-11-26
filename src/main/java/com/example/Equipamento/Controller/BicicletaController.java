package com.example.Equipamento.Controller;

import com.example.Equipamento.Dto.IncluirBicicletaDTO;
import com.example.Equipamento.Dto.RetirarBicicletaDTO;
import com.example.Equipamento.Model.Bicicleta;
import com.example.Equipamento.Repository.BicicletaRepository;
import com.example.Equipamento.Service.BicicletaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bicicleta")
@RequiredArgsConstructor
public class BicicletaController {

    private final BicicletaService bicicletaService;
    private final BicicletaRepository bicicletaRepository;

    // POST /bicicleta  – retorna Bicicleta (Swagger: 200 + Bicicleta)
    @PostMapping
    public ResponseEntity<Bicicleta> incluirBicicleta(@Valid @RequestBody Bicicleta bicicleta) {
        Bicicleta salva = bicicletaService.incluirBicicleta(bicicleta);
        return ResponseEntity.ok(salva);
    }

    // GET /bicicleta – lista de bicicletas
    @GetMapping
    public ResponseEntity<List<Bicicleta>> listarBicicletas() {
        List<Bicicleta> bicicletas = bicicletaRepository.findAll();
        return ResponseEntity.ok(bicicletas);
    }

    // GET /bicicleta/{idBicicleta}
    @GetMapping("/{idBicicleta}")
    public ResponseEntity<Bicicleta> buscarPorId(@PathVariable Integer idBicicleta) {
        return ResponseEntity.ok(bicicletaService.buscarPorId(idBicicleta));
    }

    // DELETE /bicicleta/{idBicicleta} – 200 sem corpo
    @DeleteMapping("/{idBicicleta}")
    public ResponseEntity<Void> deletarBicicleta(@PathVariable Integer idBicicleta) {
        bicicletaService.deletarBicicleta(idBicicleta);
        return ResponseEntity.ok().build();
    }

    // PUT /bicicleta/{idBicicleta} – 200 + Bicicleta atualizada
    @PutMapping("/{idBicicleta}")
    public ResponseEntity<Bicicleta> atualizarBicicletaPorId(
            @PathVariable Integer idBicicleta,
            @RequestBody Bicicleta bicicleta) {

        Bicicleta atualizada = bicicletaService.atualizarBicicletaPorId(idBicicleta, bicicleta);
        return ResponseEntity.ok(atualizada);
    }

    // POST /bicicleta/integrarNaRede – 200 sem corpo (Swagger: Dados cadastrados)
    @PostMapping("/integrarNaRede")
    public ResponseEntity<Void> incluirNaRede(@RequestBody IncluirBicicletaDTO dto) {
        bicicletaService.incluirBicicletaNaRede(dto);
        return ResponseEntity.ok().build();
    }

    // POST /bicicleta/retirarDaRede – 200 sem corpo (Swagger: Dados cadastrados)
    @PostMapping("/retirarDaRede")
    public ResponseEntity<Void> retirarDaRede(@RequestBody RetirarBicicletaDTO dto) {
        bicicletaService.retirarBicicleta(dto);
        return ResponseEntity.ok().build();
    }

    // POST /bicicleta/{idBicicleta}/status/{acao} – 200 + Bicicleta
    @PostMapping("/{idBicicleta}/status/{acao}")
    public ResponseEntity<Bicicleta> alterarStatusBicicleta(
            @PathVariable Integer idBicicleta,
            @PathVariable String acao) {

        Bicicleta atualizada = bicicletaService.alterarStatus(idBicicleta, acao);
        return ResponseEntity.ok(atualizada);
    }
}
