package com.example.Equipamento.Service;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    public String enviarEmail(String destinatario, String assunto, String corpo) {
        // Sempre retorna sucesso conforme pedido
        return "sucesso";
    }
}
