package com.example.Equipamento.Service;

import com.example.Equipamento.Model.Bicicleta;
import com.example.Equipamento.Model.Tranca;
import com.example.Equipamento.Repository.BicicletaRepository;
import com.example.Equipamento.Repository.TrancaRepository;
import com.example.Equipamento.Repository.TotemRepository;
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
class TrancaServiceTest {

    @Mock private TrancaRepository trancaRepository;
    @Mock private BicicletaRepository bicicletaRepository;
    @Mock private TotemRepository totemRepository;

    @InjectMocks private TrancaService trancaService;

    private Tranca tranca;
    private Bicicleta bicicleta;

    @BeforeEach
    void setUp() {
        tranca = new Tranca();
        tranca.setId(1);
        tranca.setNumero("TR-1");
        tranca.setStatus("livre");

        bicicleta = new Bicicleta();
        bicicleta.setId(10);
        bicicleta.setNumero("BIC-10");
        bicicleta.setStatus("nova");
    }

    @Test
    void deveSalvarTrancaComSucesso() {
        trancaService.salvarTranca(tranca);
        verify(trancaRepository, times(1)).saveAndFlush(tranca);
    }

    @Test
    void deveAtualizarTrancaQuandoNumeroNaoMuda() {
        when(trancaRepository.findById(1)).thenReturn(Optional.of(tranca));

        Tranca req = new Tranca();
        req.setStatus("ocupada");
        req.setModelo("T-Lock");

        trancaService.atualizarTrancaPorId(1, req);

        verify(trancaRepository, times(1)).saveAndFlush(tranca);
        assertThat(tranca.getStatus()).isEqualTo("ocupada");
        assertThat(tranca.getModelo()).isEqualTo("T-Lock");
    }

    @Test
    void deveLancarErroQuandoTrancaNaoEncontrada() {
        when(trancaRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trancaService.atualizarTrancaPorId(99, new Tranca()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Tranca não encontrada");
    }

    @Test
    void deveTrancarBicicletaComSucesso() {
        when(trancaRepository.findById(1)).thenReturn(Optional.of(tranca));
        when(bicicletaRepository.findByNumero("BIC-10")).thenReturn(Optional.of(bicicleta));
        when(trancaRepository.existsByBicicletaId(10)).thenReturn(false);

        trancaService.trancar(1, "BIC-10");

        assertThat(tranca.getStatus()).isEqualTo("ocupada");
        assertThat(tranca.getBicicleta()).isEqualTo(bicicleta);
        assertThat(bicicleta.getStatus()).isEqualTo("travada");
        verify(trancaRepository, times(1)).saveAndFlush(tranca);
        verify(bicicletaRepository, times(1)).saveAndFlush(bicicleta);
    }

    @Test
    void deveDestrancarTrancaComSucesso() {
        tranca.setStatus("ocupada");
        tranca.setBicicleta(bicicleta);
        when(trancaRepository.findById(1)).thenReturn(Optional.of(tranca));

        trancaService.destrancar(1);

        assertThat(tranca.getStatus()).isEqualTo("livre");
        assertThat(tranca.getBicicleta()).isNull();
        assertThat(bicicleta.getStatus()).isEqualTo("disponivel");
        verify(trancaRepository, times(1)).saveAndFlush(tranca);
    }
}
