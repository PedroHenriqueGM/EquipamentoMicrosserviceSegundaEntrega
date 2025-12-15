package com.example.Equipamento.Dto;

public class IncluirBicicletaDTO {
    private Long idBicicleta;
    private Long idTranca;
    private String idReparador;

    public Long getIdBicicleta() {
        return idBicicleta;
    }

    public void setIdBicicleta(Long idBicicleta) {
        this.idBicicleta = idBicicleta;
    }

    public Long getIdTranca() {
        return idTranca;
    }

    public void setIdTranca(Long idTranca) {
        this.idTranca = idTranca;
    }

    public String getIdReparador() {
        return idReparador;
    }

    public void setIdReparador(String idReparador) {
        this.idReparador = idReparador;
    }
}
