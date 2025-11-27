package com.example.Equipamento.Model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "totens")
public class Totem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String localizacao;

    @Column(nullable = false)
    private String descricao;

    @OneToMany(mappedBy = "totem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tranca> trancas = new ArrayList<>();

    public Long getId() { return id; }
    public String getLocalizacao() { return localizacao; }
    public void setLocalizacao(String localizacao) { this.localizacao = localizacao; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public List<Tranca> getTrancas() { return trancas; }
    public void setTrancas(List<Tranca> trancas) { this.trancas = trancas; }

    public void setId(long id) {
        this.id = id;
    }
}
