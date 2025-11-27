package com.example.Equipamento.Dto;

public class IncluirBicicletaDTO {
    private Long idBicicleta;
    private Long idTranca;
    private Long idReparador; 

    public Long getIdBicicleta() {
        return idBicicleta;
    }

    public void setIdBicicicleta(Long idBicicleta) {
        this.idBicicleta = idBicicleta;
    }

    public Long getIdTranca() {
        return idTranca;
    }

    public void setIdTranca(Long idTranca) {
        this.idTranca = idTranca;
    }

    public Long getIdReparador() {
        return idReparador;
    }

    public void setIdReparador(Long idReparador) {
        this.idReparador = idReparador;
    }
}
