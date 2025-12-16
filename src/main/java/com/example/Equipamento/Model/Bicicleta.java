package com.example.Equipamento.Model;

import com.example.Equipamento.Model.enums.StatusBicicleta;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "bicicleta")
@Entity
public class Bicicleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "numero", unique = true)
    private int numero;

    @Enumerated(EnumType.STRING)
    private StatusBicicleta status;

    @Column(name = "marca")
    private String marca;

    @Column(name = "modelo")
    private String modelo;

    @Column(name = "localização")
    private String localizacao;

    @Column(name = "ano")
    private String ano;

    @Column(name = "reparador")
    private String reparador;

    public Bicicleta(int bicicletaId) {
    }
}
