package com.example.Equipamento.Dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
public class FuncionarioDTO {
    private String matricula;
    private String nome;
    private String email;
    private String cpf;
    private String funcao;
    private int idade;
}
