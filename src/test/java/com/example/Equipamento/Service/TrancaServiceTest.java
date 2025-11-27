package com.example.Equipamento.Service;

import com.example.Equipamento.Dto.IntegrarTrancaNaRedeDTO;
import com.example.Equipamento.Dto.RetirarTrancaDTO;
import com.example.Equipamento.Model.Bicicleta;
import com.example.Equipamento.Model.Totem;
import com.example.Equipamento.Model.Tranca;
import com.example.Equipamento.Model.enums.StatusBicicleta;
import com.example.Equipamento.Model.enums.StatusTranca;
import com.example.Equipamento.Repository.BicicletaRepository;
import com.example.Equipamento.Repository.TrancaRepository;
import com.example.Equipamento.Repository.TotemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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

    private static final String MODELO = "NovoModelo";

    @BeforeEach
    void setUp() {
        tranca = new Tranca();
        tranca.setId(1);
        tranca.setNumero("TR-1");
        tranca.setStatus(StatusTranca.LIVRE);
        tranca.setModelo("Modelo X");
        tranca.setAno("2023");

        bicicleta = new Bicicleta();
        bicicleta.setId(10);
        bicicleta.setNumero("BIC-10");
        bicicleta.setStatus(StatusBicicleta.NOVA);
        bicicleta.setMarca("Caloi");
        bicicleta.setModelo("Elite");
        bicicleta.setAno("2023");

        totem = new Totem();
        totem.setId(1);
        // não precisamos setar trancas aqui; o service usa findByTotemId no repo
    }

    // ============================
    // salvarTranca
    // ============================

    @Test
    void deveSalvarTrancaComNumeroGeradoEStatusNova() {
        when(trancaRepository.saveAndFlush(any(Tranca.class)))
                .thenAnswer(invocation -> {
                    Tranca t = invocation.getArgument(0);
                    if (t.getId() == 0) {
                        t.setId(5);
                    }
                    return t;
                });

        Tranca nova = new Tranca();
        nova.setModelo("T-LOCK");
        nova.setAno("2024");

        Tranca salva = trancaService.salvarTranca(nova);

        assertThat(salva.getId()).isEqualTo(5);
        assertThat(salva.getStatus()).isEqualTo(StatusTranca.NOVA);
        assertThat(salva.getNumero()).isEqualTo("TR-5");

        verify(trancaRepository, atLeast(2)).saveAndFlush(any(Tranca.class));
    }

    @Test
    void deveLancarErroQuandoCamposObrigatoriosNaoForemInformadosAoSalvar() {
        Tranca invalida = new Tranca();
        invalida.setModelo(null);
        invalida.setAno(null);

        assertThatThrownBy(() -> trancaService.salvarTranca(invalida))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Modelo e ano são obrigatórios");

        verify(trancaRepository, never()).saveAndFlush(any());
    }

    // ============================
    // deletarTranca
    // ============================

    @Test
    void naoDevePermitirExcluirTrancaComBicicletaAssociada() {
        tranca.setBicicleta(bicicleta);
        when(trancaRepository.findById(1)).thenReturn(Optional.of(tranca));

        assertThatThrownBy(() -> trancaService.deletarTranca(1))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("tranca com bicicleta associada não pode ser excluída");

        verify(trancaRepository, never()).saveAndFlush(any());
    }

    @Test
    void deveSoftDeletarTrancaSemBicicleta() {
        tranca.setBicicleta(null);
        when(trancaRepository.findById(1)).thenReturn(Optional.of(tranca));

        trancaService.deletarTranca(1);

        assertThat(tranca.getStatus()).isEqualTo(StatusTranca.EXCLUIDA);
        verify(trancaRepository, times(1)).saveAndFlush(tranca);
    }

    // ============================
    // atualizarTrancaPorId
    // ============================

    @Test
    void deveAtualizarTrancaQuandoCamposValidos() {
        when(trancaRepository.findById(1)).thenReturn(Optional.of(tranca));

        Tranca req = new Tranca();
        req.setModelo(MODELO);
        req.setAno("2025");
        req.setLocalizacao("Totem Norte");

        Tranca atualizada = trancaService.atualizarTrancaPorId(1, req);

        assertThat(atualizada.getModelo()).isEqualTo(MODELO);
        assertThat(atualizada.getAno()).isEqualTo("2025");
        assertThat(atualizada.getLocalizacao()).isEqualTo("Totem Norte");
        // numero e status não mudam
        assertThat(atualizada.getNumero()).isEqualTo("TR-1");
        assertThat(atualizada.getStatus()).isEqualTo(StatusTranca.LIVRE);

        verify(trancaRepository, times(1)).saveAndFlush(tranca);
    }

    @Test
    void naoDevePermitirAlterarNumeroDaTranca() {
        when(trancaRepository.findById(1)).thenReturn(Optional.of(tranca));

        Tranca req = new Tranca();
        req.setModelo(MODELO);
        req.setAno("2025");
        req.setNumero("OUTRO");

        assertThatThrownBy(() -> trancaService.atualizarTrancaPorId(1, req))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("o número da tranca não pode ser alterado");

        verify(trancaRepository, never()).saveAndFlush(tranca);
    }

    // ============================
    // trancar
    // ============================

    @Test
    void deveTrancarTrancaLivreComBicicletaValida() {
        when(trancaRepository.findById(1)).thenReturn(Optional.of(tranca));
        when(bicicletaRepository.findById(10)).thenReturn(Optional.of(bicicleta));
        when(trancaRepository.existsByBicicletaId(10)).thenReturn(false);

        Tranca result = trancaService.trancar(1, 10);

        assertThat(result.getStatus()).isEqualTo(StatusTranca.OCUPADA);
        assertThat(result.getBicicleta()).isEqualTo(bicicleta);
        assertThat(bicicleta.getStatus()).isEqualTo(StatusBicicleta.DISPONIVEL);

        verify(trancaRepository, times(1)).saveAndFlush(tranca);
        verify(bicicletaRepository, times(1)).saveAndFlush(bicicleta);
    }

    @Test
    void naoDeveTrancarQuandoTrancaNaoEstaLivre() {
        tranca.setStatus(StatusTranca.OCUPADA);
        when(trancaRepository.findById(1)).thenReturn(Optional.of(tranca));

        assertThatThrownBy(() -> trancaService.trancar(1, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("A tranca só pode ser TRANCADA quando está LIVRE");
    }

    // ============================
    // destrancar
    // ============================

    @Test
    void deveDestrancarTrancaOcupadaComBicicletaCorreta() {
        tranca.setStatus(StatusTranca.OCUPADA);
        tranca.setBicicleta(bicicleta);
        when(trancaRepository.findById(1)).thenReturn(Optional.of(tranca));

        Tranca result = trancaService.destrancar(1, bicicleta.getId());

        assertThat(result.getStatus()).isEqualTo(StatusTranca.LIVRE);
        assertThat(result.getBicicleta()).isNull();
        assertThat(bicicleta.getStatus()).isEqualTo(StatusBicicleta.DISPONIVEL);

        verify(trancaRepository, times(1)).saveAndFlush(tranca);
        verify(bicicletaRepository, times(1)).saveAndFlush(bicicleta);
    }

    // ============================
    // incluirTrancaNaRede
    // ============================

    @Test
    void deveIncluirTrancaNaRedeComSucesso() {
        tranca.setStatus(StatusTranca.NOVA);
        when(totemRepository.findById(1L)).thenReturn(Optional.of(totem));
        when(trancaRepository.findById(1)).thenReturn(Optional.of(tranca));
        when(emailService.enviarEmail(anyString(), anyString(), anyString()))
                .thenReturn("sucesso");

        IntegrarTrancaNaRedeDTO dto = new IntegrarTrancaNaRedeDTO();
        dto.setIdTotem(1L);
        dto.setIdTranca(1);
        dto.setIdFuncionario(123L);

        trancaService.incluirTrancaNaRede(dto);

        assertThat(tranca.getTotem()).isEqualTo(totem);
        assertThat(tranca.getStatus()).isEqualTo(StatusTranca.LIVRE);

        verify(trancaRepository, times(1)).saveAndFlush(tranca);
        verify(totemRepository, times(1)).saveAndFlush(totem);
        verify(emailService, times(1)).enviarEmail(anyString(), anyString(), anyString());
    }

    // ============================
    // retirarTranca
    // ============================

    @Test
    void deveRetirarTrancaParaReparo() {
        tranca.setStatus(StatusTranca.REPARO_SOLICITADO);
        tranca.setTotem(totem);
        when(trancaRepository.findById(1)).thenReturn(Optional.of(tranca));
        when(emailService.enviarEmail(anyString(), anyString(), anyString()))
                .thenReturn("sucesso");

        RetirarTrancaDTO dto = new RetirarTrancaDTO();
        dto.setIdTranca(1L);
        // dto.setIdTotem(1L);
        dto.setIdFuncionario(123L);
        dto.setStatusAcaoReparador("EM_REPARO");

        trancaService.retirarTranca(dto);

        assertThat(tranca.getTotem()).isNull();
        assertThat(tranca.getStatus()).isEqualTo(StatusTranca.EM_REPARO);
        verify(trancaRepository, times(1)).saveAndFlush(tranca);
        verify(emailService, times(1)).enviarEmail(anyString(), anyString(), anyString());
    }


    // ============================
    // alterarStatus
    // ============================

    @Test
    void deveAlterarStatusParaReparoSolicitado() {
        when(trancaRepository.findById(1)).thenReturn(Optional.of(tranca));
        when(trancaRepository.saveAndFlush(any(Tranca.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Tranca result = trancaService.alterarStatus(1, "REPARO_SOLICITADO");

        assertThat(result.getStatus()).isEqualTo(StatusTranca.REPARO_SOLICITADO);
        verify(trancaRepository, times(1)).saveAndFlush(tranca);
    }

    // ============================
    // buscarBicicletaDaTranca
    // ============================

    @Test
    void deveLancarErroQuandoNaoHaBicicletaNaTranca() {
        tranca.setBicicleta(null);
        when(trancaRepository.findById(1)).thenReturn(Optional.of(tranca));

        assertThatThrownBy(() -> trancaService.buscarBicicletaDaTranca(1))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Nenhuma bicicleta está associada a esta tranca");
    }

    // ============================
    // listarTrancasDoTotem
    // ============================

    @Test
    void deveListarTrancasDoTotem() {
        when(totemRepository.existsById(1L)).thenReturn(true);
        when(trancaRepository.findByTotemId(1L)).thenReturn(List.of(tranca));

        List<Tranca> resultado = trancaService.listarTrancasDoTotem(1L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0)).isEqualTo(tranca);
    }
}
