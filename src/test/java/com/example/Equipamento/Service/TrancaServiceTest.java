package com.example.Equipamento.Service;

import com.example.Equipamento.Dto.IntegrarTrancaNaRedeDTO;
import com.example.Equipamento.Dto.RetirarTrancaDTO;
import com.example.Equipamento.Model.Bicicleta;
import com.example.Equipamento.Model.Totem;
import com.example.Equipamento.Model.Tranca;
import com.example.Equipamento.Model.enums.StatusBicicleta;
import com.example.Equipamento.Model.enums.StatusTranca;
import com.example.Equipamento.Repository.BicicletaRepository;
import com.example.Equipamento.Repository.TotemRepository;
import com.example.Equipamento.Repository.TrancaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrancaServiceTest {

    @Mock
    private TrancaRepository trancaRepository;

    @Mock
    private BicicletaRepository bicicletaRepository;

    @Mock
    private TotemRepository totemRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private TrancaService trancaService;

    private Tranca tranca;
    private Bicicleta bicicleta;
    private Totem totem;

    @BeforeEach
    void setUp() {
        tranca = new Tranca();
        tranca.setId(1);
        tranca.setNumero("TR-1");
        tranca.setStatus(StatusTranca.LIVRE);

        bicicleta = new Bicicleta();
        bicicleta.setId(10);
        bicicleta.setNumero("BIC-10");
        bicicleta.setStatus(StatusBicicleta.NOVA);

        totem = new Totem();
        // não precisamos setar o id aqui (Hibernate faria isso)
    }

    // =========================
    // TRANCAR
    // =========================
    @Test
    void deveTrancarComBicicletaComSucesso() {
        when(trancaRepository.findById(1)).thenReturn(Optional.of(tranca));
        when(bicicletaRepository.findById(10)).thenReturn(Optional.of(bicicleta));
        when(trancaRepository.existsByBicicletaId(10)).thenReturn(false);

        Tranca result = trancaService.trancar(1, 10);

        assertThat(result.getStatus()).isEqualTo(StatusTranca.OCUPADA);
        assertThat(result.getBicicleta()).isEqualTo(bicicleta);

        verify(trancaRepository).saveAndFlush(tranca);
        verify(bicicletaRepository).saveAndFlush(bicicleta);
    }

    // =========================
    // DESTRANCAR
    // =========================
    @Test
    void deveDestrancarComSucesso() {
        tranca.setStatus(StatusTranca.OCUPADA);
        tranca.setBicicleta(bicicleta);

        when(trancaRepository.findById(1)).thenReturn(Optional.of(tranca));

        Tranca result = trancaService.destrancar(1, 10);

        assertThat(result.getStatus()).isEqualTo(StatusTranca.LIVRE);
        assertThat(result.getBicicleta()).isNull();

        verify(trancaRepository).saveAndFlush(tranca);
        verify(bicicletaRepository).saveAndFlush(bicicleta);
    }

    // =========================
    // INTEGRAR TRANCA NA REDE
    // =========================
    @Test
    void deveIntegrarTrancaNaRede() {
        tranca.setStatus(StatusTranca.NOVA);

        IntegrarTrancaNaRedeDTO dto = new IntegrarTrancaNaRedeDTO();
        dto.setIdTotem(1L);
        dto.setIdTranca(1);
        dto.setIdFuncionario(99L);

        when(totemRepository.findById(1L)).thenReturn(Optional.of(totem));
        when(trancaRepository.findById(1)).thenReturn(Optional.of(tranca));
        when(emailService.enviarEmail(any(), any(), any())).thenReturn("sucesso");

        trancaService.incluirTrancaNaRede(dto);

        assertThat(tranca.getStatus()).isEqualTo(StatusTranca.LIVRE);
        assertThat(tranca.getTotem()).isEqualTo(totem);

        verify(trancaRepository).saveAndFlush(tranca);
        verify(totemRepository).saveAndFlush(totem);
    }

    // =========================
    // RETIRAR TRANCA PARA REPARO
    // =========================
    @Test
    void deveRetirarTrancaParaReparo() {
        // Tranca em REPARO_SOLICITADO associada a um totem qualquer
        Tranca tranca = new Tranca();
        tranca.setId(1);
        tranca.setStatus(StatusTranca.REPARO_SOLICITADO);

        Totem totem = new Totem();
        tranca.setTotem(totem); // não precisamos de id aqui

        when(trancaRepository.findById(1)).thenReturn(Optional.of(tranca));
        when(emailService.enviarEmail(anyString(), anyString(), anyString()))
                .thenReturn("sucesso");

        // DTO sem idTotem → não entra no if (dto.getIdTotem() != null)
        RetirarTrancaDTO dto = new RetirarTrancaDTO();
        dto.setIdTranca(1L);
        // dto.setIdTotem(null); // opcional, já fica null por padrão
        dto.setIdFuncionario(99L);
        dto.setStatusAcaoReparador("EM_REPARO");

        trancaService.retirarTranca(dto);

        // Verifica que a tranca foi marcada para EM_REPARO
        assertThat(tranca.getStatus()).isEqualTo(StatusTranca.EM_REPARO);
        // e desassociada do totem
        assertThat(tranca.getTotem()).isNull();
    }


    // =========================
    // BUSCAR BICICLETA DA TRANCA
    // =========================
    @Test
    void deveBuscarBicicletaDaTranca() {
        tranca.setBicicleta(bicicleta);
        when(trancaRepository.findById(1)).thenReturn(Optional.of(tranca));

        Bicicleta result = trancaService.buscarBicicletaDaTranca(1);

        assertThat(result).isEqualTo(bicicleta);
    }

    @Test
    void deveLancarErroQuandoNaoExisteBicicletaNaTranca() {
        when(trancaRepository.findById(1)).thenReturn(Optional.of(tranca));

        assertThatThrownBy(() -> trancaService.buscarBicicletaDaTranca(1))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Nenhuma bicicleta está associada");
    }

    @Test
    void deveFalharSeIdTotemForDiferente() {
        Tranca tranca = new Tranca();
        tranca.setId(1);
        tranca.setStatus(StatusTranca.REPARO_SOLICITADO);

        Totem totemMock = mock(Totem.class);
        when(totemMock.getId()).thenReturn(1L); // 👈 evita o NullPointer
        tranca.setTotem(totemMock);

        when(trancaRepository.findById(1)).thenReturn(Optional.of(tranca));

        RetirarTrancaDTO dto = new RetirarTrancaDTO();
        dto.setIdTranca(1L);
        dto.setIdTotem(2L); // diferente de 1 → deve falhar
        dto.setIdFuncionario(99L);
        dto.setStatusAcaoReparador("EM_REPARO");

        assertThatThrownBy(() -> trancaService.retirarTranca(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("A tranca não pertence ao totem informado");
    }

}
