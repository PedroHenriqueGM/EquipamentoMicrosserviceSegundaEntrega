package com.example.Equipamento.Dto;

public class RetirarTrancaDTO {

    private Long idTotem;
    private Long idTranca;
    private String idReparador;
    // "APOSENTADA" ou "EM_REPARO"
    private String statusAcaoReparador;

    public Long getIdTotem() {
        return idTotem;
    }

    public void setIdTotem(Long idTotem) {
        this.idTotem = idTotem;
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

    public String getStatusAcaoReparador() {
        return statusAcaoReparador;
    }

    public void setStatusAcaoReparador(String statusAcaoReparador) {
        this.statusAcaoReparador = statusAcaoReparador;
    }
}
