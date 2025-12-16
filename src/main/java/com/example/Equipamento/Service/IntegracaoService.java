package com.example.Equipamento.Service;

import com.example.Equipamento.Dto.EmailDTO;
import com.example.Equipamento.Dto.FuncionarioDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

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


    public void enviarEmail(EmailDTO email) {

        // Monte APENAS o que o externo deve aceitar (mas o nome exato pode ser outro!)
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", email.getEmail());
        payload.put("assunto", email.getAssunto());
        payload.put("mensagem", email.getMensagem());

        try {
            webClient.post()
                    .uri(serviceCUrl + "/enviarEmail")
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .accept(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            resp -> resp.bodyToMono(String.class).map(body ->
                                    new RuntimeException("Erro EmailService " + resp.statusCode() + " body=" + body)
                            )
                    )
                    .toBodilessEntity()
                    .block();

        } catch (ResponseStatusException e) {
            throw e; // mantém status/body
        } catch (Exception e) {
            throw new ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Falha chamando serviço de e-mail. Detalhe: " + e.getMessage()
            );
        }
    }
}

