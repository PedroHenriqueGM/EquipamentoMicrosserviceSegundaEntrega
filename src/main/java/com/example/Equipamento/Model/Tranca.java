package com.example.Equipamento.Model;

import com.example.Equipamento.Model.enums.StatusTranca;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "tranca")
@Entity
public class Tranca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "numero", unique = true)
    //@NotNull(message = "O número da tranca é obrigatório.")
    private int numero;

    @Enumerated(EnumType.STRING)
    private StatusTranca status;

    @Column(name = "modelo")
    private String modelo;

    @Column(name = "localização")
    private String localizacao;

    @Column(name = "ano")
    private String ano;

    @Column(name = "reparador")
    private String reparador;

    // === R4: vínculo 1:1 com Bicicleta (tranca ocupada por uma bicicleta) ===
    @OneToOne
    @JoinColumn(name = "bicicleta_id", unique = true)
    @JsonIgnore // evita loop em respostas JSON
    private Bicicleta bicicleta;

    @ManyToOne
    @JoinColumn(name = "totem_id") 
    @JsonIgnore 
    private Totem totem;
}
