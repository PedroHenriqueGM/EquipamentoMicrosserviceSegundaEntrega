package com.example.Equipamento.Dto;

public class RetirarBicicletaDTO {

    private Long idTranca;
    private Long idBicicleta;
    private String idReparador;
    // Valores esperados: "APOSENTADA" ou "EM_REPARO"
    private String statusAcaoReparador;

    public Long getIdTranca() {
        return idTranca;
    }

    public void setIdTranca(Long idTranca) {
        this.idTranca = idTranca;
    }

    public Long getIdBicicleta() {
        return idBicicleta;
    }

    public void setIdBicicleta(Long idBicicleta) {
        this.idBicicleta = idBicicleta;
    }

    public String getIdReparador() {
        return idReparador;
    }

    public void setIdReparador(String idReparador) {
        this.idReparador = idReparador;
    }

    public String getStatusAcaoReparador() {
        return statusAcaoReparador;
    }

    public void setStatusAcaoReparador(String statusAcaoReparador) {
        this.statusAcaoReparador = statusAcaoReparador;
    }
}
