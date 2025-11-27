package com.example.Equipamento.Dto;
import jakarta.validation.constraints.NotBlank;
public record NovoTotemDTO(
        @NotBlank(message = "Localização é obrigatória")
        String localizacao,

        @NotBlank(message = "Descrição é obrigatória")
        String descricao
) {}

