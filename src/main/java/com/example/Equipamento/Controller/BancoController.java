package com.example.Equipamento.Controller;

import com.example.Equipamento.Service.BancoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BancoController {

    // Injeção do serviço que realiza a limpeza do banco
    @Autowired
    private BancoService bancoService;

    // Endpoint que chama o serviço para restaurar o banco
    @GetMapping("/restaurarBanco")
    public ResponseEntity<String> restaurarBanco() {
        bancoService.restaurarBanco();
        return ResponseEntity.ok("Banco restaurado com sucesso.");
    }
}
