package com.example.Equipamento.Service;

import com.example.Equipamento.Dto.EmailDTO;
import com.example.Equipamento.Dto.FuncionarioDTO;
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

    @InjectMocks
    private TrancaService trancaService;

    @Mock
    private IntegracaoService integracaoService;


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
        totem.setId(1L);
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
    void deveIntegrarTrancaNovaNaRede() {
        tranca.setStatus(StatusTranca.NOVA);

        IntegrarTrancaNaRedeDTO dto = new IntegrarTrancaNaRedeDTO();
        dto.setIdTotem(1L);
        dto.setIdTranca(1);
        dto.setIdReparador("99");

        FuncionarioDTO funcionario = new FuncionarioDTO();
        funcionario.setEmail("reparador@email.com");

        when(totemRepository.findById(1L)).thenReturn(Optional.of(totem));
        when(trancaRepository.findById(1)).thenReturn(Optional.of(tranca));
        when(integracaoService.buscarFuncionario("99")).thenReturn(funcionario);
        doNothing().when(integracaoService).enviarEmail(any());

        trancaService.incluirTrancaNaRede(dto);

        assertThat(tranca.getStatus()).isEqualTo(StatusTranca.LIVRE);
        assertThat(tranca.getTotem()).isEqualTo(totem);

        verify(trancaRepository).saveAndFlush(tranca);
        verify(totemRepository).saveAndFlush(totem);
        verify(integracaoService).enviarEmail(any());
    }


    // =========================
    // RETIRAR TRANCA PARA REPARO
    // =========================
    @Test
    void deveRetirarTrancaParaReparo() {
        tranca.setStatus(StatusTranca.REPARO_SOLICITADO);
        tranca.setTotem(totem);

        RetirarTrancaDTO dto = new RetirarTrancaDTO();
        dto.setIdTranca(1L);
        dto.setIdTotem(1L);
        dto.setIdReparador("88");
        dto.setStatusAcaoReparador("EM_REPARO");

        FuncionarioDTO funcionario = new FuncionarioDTO();
        funcionario.setEmail("rep@email.com");

        when(trancaRepository.findById(1)).thenReturn(Optional.of(tranca));
        when(integracaoService.buscarFuncionario("88")).thenReturn(funcionario);
        doNothing().when(integracaoService).enviarEmail(any());

        trancaService.retirarTranca(dto);

        assertThat(tranca.getStatus()).isEqualTo(StatusTranca.EM_REPARO);
        assertThat(tranca.getTotem()).isNull();
        assertThat(tranca.getReparador()).isEqualTo("88");

        verify(trancaRepository).saveAndFlush(tranca);
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
        dto.setIdReparador("99L");
        dto.setStatusAcaoReparador("EM_REPARO");

        assertThatThrownBy(() -> trancaService.retirarTranca(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("A tranca não pertence ao totem informado");
    }

    @Test
    void deveFalharAoTrancarTrancaInexistente() {
        when(trancaRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trancaService.trancar(1, 10))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Tranca não encontrada");
    }

    @Test
    void deveFalharAoTrancarQuandoStatusNaoForLivre() {
        tranca.setStatus(StatusTranca.EM_REPARO);

        when(trancaRepository.findById(1)).thenReturn(Optional.of(tranca));

        assertThatThrownBy(() -> trancaService.trancar(1, 10))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("só pode ser TRANCADA quando está LIVRE");
    }



    @Test
    void deveFalharAoTrancarComBicicletaInexistente() {
        when(trancaRepository.findById(1)).thenReturn(Optional.of(tranca));
        when(bicicletaRepository.findById(10)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trancaService.trancar(1, 10))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Bicicleta não encontrada");
    }

    @Test
    void deveFalharAoDestrancarTrancaInexistente() {
        when(trancaRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trancaService.destrancar(1, 10))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Tranca não encontrada");
    }

    @Test
    void deveFalharAoDestrancarQuandoNaoEstaOcupada() {
        tranca.setStatus(StatusTranca.LIVRE);

        when(trancaRepository.findById(1)).thenReturn(Optional.of(tranca));

        assertThatThrownBy(() -> trancaService.destrancar(1, 10))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("só pode ser DESTRANCADA quando está OCUPADA");
    }


    @Test
    void deveFalharAoIntegrarQuandoTrancaNaoExiste() {
        IntegrarTrancaNaRedeDTO dto = new IntegrarTrancaNaRedeDTO();
        dto.setIdTotem(1L);
        dto.setIdTranca(1);
        dto.setIdReparador("99");

        when(totemRepository.findById(1L)).thenReturn(Optional.of(totem));
        when(trancaRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trancaService.incluirTrancaNaRede(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Tranca não encontrada");
    }


    @Test
    void deveFalharAoIntegrarQuandoTotemNaoExiste() {
        IntegrarTrancaNaRedeDTO dto = new IntegrarTrancaNaRedeDTO();
        dto.setIdTotem(1L);
        dto.setIdTranca(1);
        dto.setIdReparador("99");

        when(totemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trancaService.incluirTrancaNaRede(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Totem não encontrado");
    }


    @Test
    void deveFalharAoAlterarStatusComAcaoInvalida() {
        when(trancaRepository.findById(1)).thenReturn(Optional.of(tranca));

        assertThatThrownBy(() -> trancaService.alterarStatus(1, "INVALIDO"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Ação inválida");
    }

    @Test
    void deveFalharAoAlterarStatusTrancaInexistente() {
        when(trancaRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trancaService.alterarStatus(1, "BLOQUEAR"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Tranca não encontrada");
    }

    @Test
    void deveFalharAoBuscarTrancaInexistente() {
        when(trancaRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trancaService.buscarPorId(1))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void deveDeletarTranca() {
        tranca.setStatus(StatusTranca.LIVRE);

        when(trancaRepository.findById(1)).thenReturn(Optional.of(tranca));
        when(trancaRepository.saveAndFlush(any(Tranca.class))).thenReturn(tranca);

        trancaService.deletarTranca(1);

        verify(trancaRepository).saveAndFlush(tranca);
    }


    @Test
    void deveFalharAoDeletarTrancaInexistente() {
        when(trancaRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trancaService.deletarTranca(1))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void deveFalharAoTrancarBicicletaJaAssociada() {
        tranca.setStatus(StatusTranca.LIVRE);

        when(trancaRepository.findById(1)).thenReturn(Optional.of(tranca));
        when(bicicletaRepository.findById(10)).thenReturn(Optional.of(bicicleta));
        when(trancaRepository.existsByBicicletaId(10)).thenReturn(true);

        assertThatThrownBy(() -> trancaService.trancar(1, 10))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void deveFalharAoSalvarTrancaComCamposObrigatoriosNaoPreenchidos() {
        Tranca trancaInvalida = new Tranca();
        trancaInvalida.setModelo(null);  // Modelo é obrigatório
        trancaInvalida.setAno(null);     // Ano é obrigatório

        assertThatThrownBy(() -> trancaService.salvarTranca(trancaInvalida))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("R2: Modelo e ano são obrigatórios para cadastrar uma tranca.");
    }

    @Test
    void deveFalharAoDeletarTrancaComBicicletaAssociada() {
        tranca.setBicicleta(bicicleta); // Associando uma bicicleta

        when(trancaRepository.findById(1)).thenReturn(Optional.of(tranca));

        assertThatThrownBy(() -> trancaService.deletarTranca(1))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("R4: tranca com bicicleta associada não pode ser excluída.");
    }

    @Test
    void deveFalharAoAlterarStatusParaTransicaoInvalida() {
        tranca.setStatus(StatusTranca.LIVRE);
        when(trancaRepository.findById(1)).thenReturn(Optional.of(tranca));

        assertThatThrownBy(() -> trancaService.alterarStatus(1, "INVALIDO"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Ação inválida");
    }


    @Test
    void deveFalharAoIntegrarTrancaNaRedeQuandoEnvioDeEmailFalha() {

        // Arrange
        IntegrarTrancaNaRedeDTO dto = new IntegrarTrancaNaRedeDTO();
        dto.setIdTotem(1L);
        dto.setIdTranca(1);
        dto.setIdReparador("10");

        Totem totem = new Totem();
        totem.setId(1L);

        Tranca tranca = new Tranca();
        tranca.setId(1);
        tranca.setStatus(StatusTranca.NOVA);
        tranca.setNumero("TR-1");

        FuncionarioDTO funcionario = new FuncionarioDTO();
        funcionario.setEmail("reparador@email.com");

        when(totemRepository.findById(1L))
                .thenReturn(Optional.of(totem));

        when(trancaRepository.findById(1))
                .thenReturn(Optional.of(tranca));

        when(integracaoService.buscarFuncionario("10"))
                .thenReturn(funcionario);

        doThrow(new RuntimeException("erro mailgun"))
                .when(integracaoService)
                .enviarEmail(any(EmailDTO.class));

        // Act + Assert
        assertThatThrownBy(() -> trancaService.incluirTrancaNaRede(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("erro mailgun");

        // Verificações importantes para cobertura
        verify(integracaoService).buscarFuncionario("10");
        verify(integracaoService).enviarEmail(any(EmailDTO.class));
        verify(trancaRepository).saveAndFlush(tranca);
        verify(totemRepository).saveAndFlush(totem);
    }












}
