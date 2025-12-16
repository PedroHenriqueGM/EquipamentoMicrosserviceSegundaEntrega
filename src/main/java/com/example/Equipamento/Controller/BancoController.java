package com.example.Equipamento.Controller;

import com.example.Equipamento.Service.BancoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BancoController {

    private final BancoService bancoService;

    public BancoController(BancoService bancoService) {
        this.bancoService = bancoService;
    }

    @GetMapping("/restaurarBanco")
    public ResponseEntity<Void> restaurarBanco() {
        bancoService.restaurarBanco();
        return ResponseEntity.ok().build();
    }
}
