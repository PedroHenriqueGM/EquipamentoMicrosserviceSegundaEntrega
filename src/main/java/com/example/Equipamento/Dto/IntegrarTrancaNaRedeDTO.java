package com.example.Equipamento.Dto;

public class IntegrarTrancaNaRedeDTO {

    private Long idTotem;
    private int idTranca;
    private String idReparador;

    public Long getIdTotem() {
        return idTotem;
    }

    public void setIdTotem(Long idTotem) {
        this.idTotem = idTotem;
    }

    public int getIdTranca() {
        return idTranca;
    }

    public void setIdTranca(int idTranca) {
        this.idTranca = idTranca;
    }

    public String getIdReparador() {
        return idReparador;
    }

    public void setIdReparador(String idReparador) {
        this.idReparador = idReparador;
    }
}
