package com.example.Equipamento.Dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
public class EmailDTO {
    Long id;
    String email;
    String assunto;
    String mensagem;
}
