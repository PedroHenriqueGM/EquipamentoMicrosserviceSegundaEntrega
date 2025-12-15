package com.example.Equipamento.Service;

import com.example.Equipamento.Dto.IncluirBicicletaDTO;
import com.example.Equipamento.Dto.RetirarBicicletaDTO;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BicicletaServiceTest {

    @Mock
    private BicicletaRepository bicicletaRepository;

    @Mock
    private TrancaRepository trancaRepository;

    @Mock
    private TotemRepository totemRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private BicicletaService bicicletaService;

    private Bicicleta bicicleta;

    private static final String NUMERO_BICICLETA = "BIC-1";
    private static final String MARCA = "Caloi";
    private static final String MARCA2 = "Sense";
    private static final String MODELO = "Impact SL";

    @BeforeEach
    void setUp() {
        bicicleta = new Bicicleta();
        bicicleta.setId(1);
        bicicleta.setNumero(NUMERO_BICICLETA);
        bicicleta.setStatus(StatusBicicleta.NOVA);
        bicicleta.setMarca(MARCA);
        bicicleta.setModelo("Elite");
        bicicleta.setAno("2023");
        bicicleta.setLocalizacao("Totem Centro");
    }

    // ============================
    // incluirBicicleta
    // ============================

    @Test
    void deveSalvarBicicletaEAtribuirNumeroAutomaticoEStatusNova() {
        when(bicicletaRepository.saveAndFlush(any(Bicicleta.class)))
                .thenAnswer(invocation -> {
                    Bicicleta b = invocation.getArgument(0);
                    if (b.getId() == 0) {
                        b.setId(10);
                    }
                    return b;
                });

        Bicicleta nova = Bicicleta.builder()
                .marca(MARCA)
                .modelo("Elite")
                .localizacao("Centro")
                .ano("2023")
                .build();

        Bicicleta salva = bicicletaService.incluirBicicleta(nova);

        assertThat(salva.getId()).isEqualTo(10);
        assertThat(salva.getNumero()).isEqualTo("BIC-10");
        assertThat(salva.getStatus()).isEqualTo(StatusBicicleta.NOVA);

        verify(bicicletaRepository, atLeast(2)).saveAndFlush(any(Bicicleta.class));
    }

    @Test
    void deveLancarErroQuandoCamposObrigatoriosNaoForemInformadosAoIncluir() {
        Bicicleta invalida = new Bicicleta();
        invalida.setMarca(null);
        invalida.setModelo("X");
        invalida.setAno("2020");

        assertThatThrownBy(() -> bicicletaService.incluirBicicleta(invalida))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("R2: Marca, modelo e ano são obrigatórios");

        verify(bicicletaRepository, never()).saveAndFlush(any());
    }

    // ============================
    // buscarPorId
    // ============================

    @Test
    void deveBuscarPorIdComSucesso() {
        when(bicicletaRepository.findById(1)).thenReturn(Optional.of(bicicleta));

        Bicicleta encontrada = bicicletaService.buscarPorId(1);

        assertThat(encontrada.getNumero()).isEqualTo(NUMERO_BICICLETA);
        assertThat(encontrada.getMarca()).isEqualTo(MARCA);
        verify(bicicletaRepository, times(1)).findById(1);
    }

    @Test
    void deveLancar404QuandoBicicletaNaoEncontrada() {
        when(bicicletaRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bicicletaService.buscarPorId(99))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Bicicleta não encontrada");
    }

    // ============================
    // atualizarBicicletaPorId
    // ============================

    @Test
    void deveAtualizarCamposPermitidos() {
        when(bicicletaRepository.findById(1)).thenReturn(Optional.of(bicicleta));

        Bicicleta req = new Bicicleta();
        req.setMarca(MARCA2);
        req.setModelo(MODELO);
        req.setAno("2024");
        req.setLocalizacao("Totem Norte");

        Bicicleta atualizada = bicicletaService.atualizarBicicletaPorId(1, req);

        assertThat(atualizada.getMarca()).isEqualTo(MARCA2);
        assertThat(atualizada.getModelo()).isEqualTo(MODELO);
        verify(bicicletaRepository, times(1)).saveAndFlush(bicicleta);
    }

    @Test
    void naoDevePermitirAlterarNumeroDaBicicleta() {
        when(bicicletaRepository.findById(1)).thenReturn(Optional.of(bicicleta));

        Bicicleta req = new Bicicleta();
        req.setMarca(MARCA2);
        req.setModelo(MODELO);
        req.setAno("2024");
        req.setNumero("OUTRO-NUMERO");

        assertThatThrownBy(() -> bicicletaService.atualizarBicicletaPorId(1, req))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("R3: o número não pode ser alterado");
    }

    @Test
    void naoDevePermitirAlterarStatusViaPut() {
        when(bicicletaRepository.findById(1)).thenReturn(Optional.of(bicicleta));

        Bicicleta req = new Bicicleta();
        req.setMarca(MARCA2);
        req.setModelo(MODELO);
        req.setAno("2024");
        req.setStatus(StatusBicicleta.DISPONIVEL);

        assertThatThrownBy(() -> bicicletaService.atualizarBicicletaPorId(1, req))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("R1: o status da bicicleta não pode ser alterado via PUT");
    }

    // ============================
    // deletarBicicleta
    // ============================

    @Test
    void deveLancarErroAoDeletarBicicletaNaoAposentada() {
        bicicleta.setStatus(StatusBicicleta.DISPONIVEL);
        when(bicicletaRepository.findById(1)).thenReturn(Optional.of(bicicleta));

        assertThatThrownBy(() -> bicicletaService.deletarBicicleta(1))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("apenas bicicletas 'APOSENTADA' podem ser excluídas");
    }

    @Test
    void deveLancarErroAoDeletarBicicletaVinculadaATranca() {
        bicicleta.setStatus(StatusBicicleta.APOSENTADA);
        when(bicicletaRepository.findById(1)).thenReturn(Optional.of(bicicleta));
        when(trancaRepository.existsByBicicletaId(1)).thenReturn(true);

        assertThatThrownBy(() -> bicicletaService.deletarBicicleta(1))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("bicicleta vinculada a uma tranca não pode ser excluída");
    }

    @Test
    void deveSoftDeletarBicicletaQuandoAposentadaESemTranca() {
        bicicleta.setStatus(StatusBicicleta.APOSENTADA);
        when(bicicletaRepository.findById(1)).thenReturn(Optional.of(bicicleta));
        when(trancaRepository.existsByBicicletaId(1)).thenReturn(false);

        bicicletaService.deletarBicicleta(1);

        assertThat(bicicleta.getStatus()).isEqualTo(StatusBicicleta.EXCLUIDA);
        verify(bicicletaRepository, times(1)).saveAndFlush(bicicleta);
    }

    // ============================
    // listarBicicletasDoTotem
    // ============================

    @Test
    void deveListarBicicletasDoTotem() {
        Totem totem = new Totem();
        Tranca tranca = new Tranca();
        tranca.setStatus(StatusTranca.OCUPADA);
        tranca.setBicicleta(bicicleta);
        totem.setTrancas(Collections.singletonList(tranca));

        when(totemRepository.findById(1L)).thenReturn(Optional.of(totem));

        List<Bicicleta> resultado = bicicletaService.listarBicicletasDoTotem(1L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getId()).isEqualTo(bicicleta.getId());
    }

    @Test
    void deveLancarErroQuandoTotemNaoEncontradoAoListarBicicletas() {
        when(totemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bicicletaService.listarBicicletasDoTotem(1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Totem não encontrado");
    }

    // ============================
    // alterarStatus
    // ============================

    @Test
    void deveAlterarStatusDaBicicletaComSucesso() {
        when(bicicletaRepository.findById(1)).thenReturn(Optional.of(bicicleta));
        when(bicicletaRepository.saveAndFlush(any(Bicicleta.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Bicicleta result = bicicletaService.alterarStatus(1, "EM_USO");

        assertThat(result.getStatus()).isEqualTo(StatusBicicleta.EM_USO);
    }

    @Test
    void deveLancarErroQuandoAcaoNaoInformadaNoAlterarStatus() {
        when(bicicletaRepository.findById(1)).thenReturn(Optional.of(bicicleta));

        assertThatThrownBy(() -> bicicletaService.alterarStatus(1, " "))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Ação não informada");
    }

    @Test
    void deveLancarErroQuandoStatusInvalidoNoAlterarStatus() {
        when(bicicletaRepository.findById(1)).thenReturn(Optional.of(bicicleta));

        ResponseStatusException ex = catchThrowableOfType(
                () -> bicicletaService.alterarStatus(1, "qualquer"),
                ResponseStatusException.class
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(ex.getReason()).contains("Status inválido");
    }

    // ====================================================================
    // NOVOS TESTES ADICIONADOS PARA AUMENTAR COBERTURA (incluirBicicletaNaRede)
    // ====================================================================
/*
    @Test
    void deveIncluirBicicletaNaRedeComSucesso() {
        // Arrange
        IncluirBicicletaDTO dto = new IncluirBicicletaDTO();
        dto.setIdBicicleta(1L);
        dto.setIdTranca(10L);
        dto.setIdReparador("99L");

        Bicicleta bike = new Bicicleta();
        bike.setId(1);
        bike.setNumero("BIC-1");
        bike.setStatus(StatusBicicleta.NOVA); // Status aceito

        Tranca tranca = new Tranca();
        tranca.setId(10);
        tranca.setStatus(StatusTranca.LIVRE); // Tranca livre

        when(bicicletaRepository.findById(1)).thenReturn(Optional.of(bike));
        when(trancaRepository.findById(10)).thenReturn(Optional.of(tranca));
        when(emailService.enviarEmail(anyString(), anyString(), anyString())).thenReturn("sucesso");

        // Act
        bicicletaService.incluirBicicletaNaRede(dto);

        // Assert
        assertThat(bike.getStatus()).isEqualTo(StatusBicicleta.DISPONIVEL);
        assertThat(tranca.getBicicleta()).isEqualTo(bike);

        verify(bicicletaRepository).saveAndFlush(bike);
        verify(trancaRepository).saveAndFlush(tranca);
        verify(emailService).enviarEmail(contains("reparador99"), anyString(), anyString());
    }

    @Test
    void naoDeveIncluirBicicletaNaRedeSeStatusInvalido() {
        IncluirBicicletaDTO dto = new IncluirBicicletaDTO();
        dto.setIdBicicleta(1L);

        Bicicleta bike = new Bicicleta();
        bike.setStatus(StatusBicicleta.EM_USO); // Status inválido para inclusão

        when(bicicletaRepository.findById(1)).thenReturn(Optional.of(bike));

        assertThatThrownBy(() -> bicicletaService.incluirBicicletaNaRede(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Bicicleta deve estar com status NOVA ou EM_REPARO");
    }

    @Test
    void naoDeveIncluirBicicletaNaRedeSeTrancaOcupada() {
        IncluirBicicletaDTO dto = new IncluirBicicletaDTO();
        dto.setIdBicicleta(1L);
        dto.setIdTranca(10L);

        Bicicleta bike = new Bicicleta();
        bike.setStatus(StatusBicicleta.NOVA);

        Tranca tranca = new Tranca();
        tranca.setStatus(StatusTranca.OCUPADA); // Tranca ocupada

        when(bicicletaRepository.findById(1)).thenReturn(Optional.of(bike));
        when(trancaRepository.findById(10)).thenReturn(Optional.of(tranca));

        assertThatThrownBy(() -> bicicletaService.incluirBicicletaNaRede(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("A tranca deve estar LIVRE");
    }

    @Test
    void deveLancarErroSeEmailFalharNaInclusaoNaRede() {
        IncluirBicicletaDTO dto = new IncluirBicicletaDTO();
        dto.setIdBicicleta(1L);
        dto.setIdTranca(10L);

        Bicicleta bike = new Bicicleta();
        bike.setStatus(StatusBicicleta.NOVA);
        Tranca tranca = new Tranca();
        tranca.setStatus(StatusTranca.LIVRE);

        when(bicicletaRepository.findById(1)).thenReturn(Optional.of(bike));
        when(trancaRepository.findById(10)).thenReturn(Optional.of(tranca));
        // Simula erro no envio do email
        when(emailService.enviarEmail(anyString(), anyString(), anyString())).thenReturn("erro");

        assertThatThrownBy(() -> bicicletaService.incluirBicicletaNaRede(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Erro ao enviar o email");
    }*/

    // ====================================================================
    // NOVOS TESTES ADICIONADOS PARA AUMENTAR COBERTURA (retirarBicicleta)
    // ====================================================================

    @Test
    void deveRetirarBicicletaParaReparoComSucesso() {
        RetirarBicicletaDTO dto = new RetirarBicicletaDTO();
        dto.setIdTranca(10L);
        dto.setIdBicicleta(1L);
        dto.setIdFuncionario(88L);
        dto.setStatusAcaoReparador("EM_REPARO");

        Bicicleta bike = new Bicicleta();
        bike.setId(1);
        bike.setStatus(StatusBicicleta.REPARO_SOLICITADO);

        Tranca tranca = new Tranca();
        tranca.setId(10);
        tranca.setNumero("T-10");
        tranca.setBicicleta(bike); // Tranca contém a bicicleta

        when(trancaRepository.findById(10)).thenReturn(Optional.of(tranca));
        when(emailService.enviarEmail(anyString(), anyString(), anyString())).thenReturn("sucesso");

        bicicletaService.retirarBicicleta(dto);

        assertThat(tranca.getBicicleta()).isNull();
        assertThat(tranca.getStatus()).isEqualTo(StatusTranca.LIVRE);
        assertThat(bike.getStatus()).isEqualTo(StatusBicicleta.EM_REPARO);

        verify(trancaRepository).saveAndFlush(tranca);
        verify(bicicletaRepository).saveAndFlush(bike);
    }

    @Test
    void deveLancarErroSeTrancaVaziaNaRetirada() {
        RetirarBicicletaDTO dto = new RetirarBicicletaDTO();
        dto.setIdTranca(10L);

        Tranca tranca = new Tranca();
        tranca.setBicicleta(null); // Tranca sem bicicleta

        when(trancaRepository.findById(10)).thenReturn(Optional.of(tranca));

        assertThatThrownBy(() -> bicicletaService.retirarBicicleta(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Não há bicicleta presa nesta tranca");
    }

    @Test
    void deveLancarErroSeIdBicicletaNaoCorresponderNaRetirada() {
        RetirarBicicletaDTO dto = new RetirarBicicletaDTO();
        dto.setIdTranca(10L);
        dto.setIdBicicleta(999L); // ID errado

        Bicicleta bike = new Bicicleta();
        bike.setId(1);

        Tranca tranca = new Tranca();
        tranca.setBicicleta(bike);

        when(trancaRepository.findById(10)).thenReturn(Optional.of(tranca));

        assertThatThrownBy(() -> bicicletaService.retirarBicicleta(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("A bicicleta informada não corresponde");
    }

    @Test
    void deveLancarErroSeStatusNaoForReparoSolicitadoNaRetirada() {
        RetirarBicicletaDTO dto = new RetirarBicicletaDTO();
        dto.setIdTranca(10L);

        Bicicleta bike = new Bicicleta();
        bike.setStatus(StatusBicicleta.DISPONIVEL); // Status inválido para retirada

        Tranca tranca = new Tranca();
        tranca.setBicicleta(bike);

        when(trancaRepository.findById(10)).thenReturn(Optional.of(tranca));

        assertThatThrownBy(() -> bicicletaService.retirarBicicleta(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("A bicicleta deve estar com status REPARO_SOLICITADO");
    }

    @Test
    void deveLancarErroSeAcaoReparadorForInvalidaNaRetirada() {
        RetirarBicicletaDTO dto = new RetirarBicicletaDTO();
        dto.setIdTranca(10L);
        dto.setStatusAcaoReparador("DESTRUIR"); // Ação inválida

        Bicicleta bike = new Bicicleta();
        bike.setStatus(StatusBicicleta.REPARO_SOLICITADO);

        Tranca tranca = new Tranca();
        tranca.setBicicleta(bike);

        when(trancaRepository.findById(10)).thenReturn(Optional.of(tranca));

        assertThatThrownBy(() -> bicicletaService.retirarBicicleta(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("statusAcaoReparador deve ser 'EM_REPARO' ou 'APOSENTADA'");
    }
}
