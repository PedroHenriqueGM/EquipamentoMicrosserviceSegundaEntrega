package com.example.Equipamento.Controller;

import com.example.Equipamento.Dto.NovoTotemDTO;
import com.example.Equipamento.Dto.TotemDTO;
import com.example.Equipamento.Model.Bicicleta;
import com.example.Equipamento.Model.Tranca;
import com.example.Equipamento.Service.BicicletaService;
import com.example.Equipamento.Service.TotemService;
import com.example.Equipamento.Service.TrancaService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/totem")
public class TotemController {

    private final TotemService totemService;
    private final TrancaService trancaService;
    private final BicicletaService bicicletaService;

    public TotemController(TotemService totemService, 
                           TrancaService trancaService, 
                           BicicletaService bicicletaService) {
        this.totemService = totemService;
        this.trancaService = trancaService;
        this.bicicletaService = bicicletaService;
    }

    @GetMapping
    public ResponseEntity<List<TotemDTO>> listarTotens() {
        return ResponseEntity.ok(totemService.listarTotens());
    }

    @PostMapping
    public ResponseEntity<TotemDTO> incluirTotem(@Valid @RequestBody NovoTotemDTO dto) {
        TotemDTO totemSalvo = totemService.incluirTotem(dto);
        URI location = URI.create("/totem/" + totemSalvo.id());
        return ResponseEntity.created(location).body(totemSalvo);
    }
   
    @DeleteMapping("/{id}")
    public ResponseEntity<String> excluirTotem(@PathVariable Long id) {
        totemService.excluirTotem(id);
        return ResponseEntity.ok("Totem excluído com sucesso.");
    }

    @GetMapping("/{idTotem}/trancas")
    public ResponseEntity<List<Tranca>> listarTrancasDoTotem(@PathVariable Long idTotem) {
        List<Tranca> trancas = trancaService.listarTrancasDoTotem(idTotem);
        return ResponseEntity.ok(trancas);
    }

    @GetMapping("/{idTotem}/bicicletas")
    public ResponseEntity<List<Bicicleta>> listarBicicletasDoTotem(@PathVariable Long idTotem) {
        List<Bicicleta> bicicletas = bicicletaService.listarBicicletasDoTotem(idTotem);
        return ResponseEntity.ok(bicicletas);
    }
}
