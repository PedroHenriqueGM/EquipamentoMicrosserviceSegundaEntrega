package com.example.Equipamento.Controller;

import com.example.Equipamento.Dto.IncluirBicicletaDTO;
import com.example.Equipamento.Dto.RetirarBicicletaDTO;
import com.example.Equipamento.Model.Bicicleta;
import com.example.Equipamento.Repository.BicicletaRepository;
import com.example.Equipamento.Service.BicicletaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/bicicleta")
@RequiredArgsConstructor
public class BicicletaController {
    private final BicicletaService bicicletaService;

    @PostMapping
    public ResponseEntity<Void> incluirBicicleta(@Valid @RequestBody Bicicleta bicicleta) {
        bicicletaService.incluirBicicleta(bicicleta);
        return ok().build();
    }

    @Autowired
    private BicicletaRepository bicicletaRepository;

    @GetMapping
    public ResponseEntity<List<Bicicleta>> listarBicicletas() {
        List<Bicicleta> bicicletas = bicicletaRepository.findAll();
        return ok(bicicletas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bicicleta> buscarPorId(@PathVariable Integer id){
        return ok(bicicletaService.buscarPorId(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletarBicicleta(@PathVariable Integer id) {
        bicicletaService.deletarBicicleta(id);
        return ResponseEntity.ok("Bicicleta excluída com sucesso.");
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> atualizarBicicletaPorId(
            @PathVariable Integer id,
            @RequestBody Bicicleta bicicleta) {
        bicicletaService.atualizarBicicletaPorId(id, bicicleta);
        return ok().build();
    }

    @PostMapping("/integrarNaRede")
    public ResponseEntity<Void> incluirNaRede(@RequestBody IncluirBicicletaDTO dto) {

        bicicletaService.incluirBicicletaNaRede(dto);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/retirarDaRede")
    public ResponseEntity<String> retirarDaRede(@RequestBody RetirarBicicletaDTO dto) {
        bicicletaService.retirarBicicleta(dto);
        return ResponseEntity.ok("Bicicleta retirada com sucesso.");
    }


}
