package com.example.Equipamento.Service;

import com.example.Equipamento.Model.Bicicleta;
import com.example.Equipamento.Repository.BicicletaRepository;
import com.example.Equipamento.Service.BicicletaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BicicletaServiceTest {

    @Mock
    private BicicletaRepository repository;
    @InjectMocks
    private BicicletaService service;

    private Bicicleta bicicleta;

    @BeforeEach
    void setUp() {
        bicicleta = new Bicicleta();
        bicicleta.setId(1);
        bicicleta.setNumero("BIC-1");
        bicicleta.setStatus("nova");
        bicicleta.setMarca("Caloi");
        bicicleta.setModelo("Elite");
        bicicleta.setAno("2023");
    }

    @Test
    void deveSalvarBicicletaEAtribuirNumeroAutomatico() {
        when(repository.saveAndFlush(any(Bicicleta.class)))
                .thenAnswer(invocation -> {
                    Bicicleta b = invocation.getArgument(0);
                    if (b.getId() == 0) b.setId(10);
                    return b;
                });

        service.incluirBicicleta(bicicleta);

        verify(repository, atLeast(2)).saveAndFlush(any(Bicicleta.class));
    }

    @Test
    void deveBuscarPorIdComSucesso() {
        when(repository.findById(1)).thenReturn(Optional.of(bicicleta));
        Bicicleta encontrada = service.buscarPorId(1);
        assertThat(encontrada.getNumero()).isEqualTo("BIC-1");
    }

    @Test
    void deveLancar404QuandoBicicletaNaoEncontrada() {
        when(repository.findById(99)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.buscarPorId(99))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Bicicleta não encontrada");
    }

    @Test
    void deveAtualizarCamposPermitidos() {
        when(repository.findById(1)).thenReturn(Optional.of(bicicleta));

        Bicicleta req = new Bicicleta();
        req.setMarca("Sense");
        req.setModelo("Impact SL");

        service.atualizarBicicletaPorId(1, req);

        assertThat(bicicleta.getMarca()).isEqualTo("Sense");
        assertThat(bicicleta.getModelo()).isEqualTo("Impact SL");
        verify(repository, times(1)).saveAndFlush(bicicleta);
    }

}