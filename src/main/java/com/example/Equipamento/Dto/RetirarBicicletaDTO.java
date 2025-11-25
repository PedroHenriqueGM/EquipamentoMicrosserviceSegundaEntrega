package com.example.Equipamento.Dto;

public class RetirarBicicletaDTO {
    private Long idTranca;
    private Long idReparador;
    private String motivo; // "reparo" ou "aposentadoria"

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

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
