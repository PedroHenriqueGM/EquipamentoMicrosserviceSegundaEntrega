package com.example.Equipamento.Service;

import com.example.Equipamento.Dto.EmailDTO;
import com.example.Equipamento.Dto.FuncionarioDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class IntegracaoService {

    private final WebClient webClient;

    @Value("${services.service-b.base-url}")
    private String serviceBUrl;

    @Value("${services.service-c.base-url}")
    private String serviceCUrl;

    public FuncionarioDTO buscarFuncionario(String matricula) {
        return webClient.get()
                .uri(serviceBUrl + "/funcionario/{matricula}", matricula)
                .retrieve()
                .bodyToMono(FuncionarioDTO.class)
                .block();
    }


    public EmailDTO enviarEmail(Long id) {
        return webClient.get()
                .uri(serviceCUrl + "/enviarEmail")
                .retrieve()
                .bodyToMono(EmailDTO.class)
                .block();
    }
}

