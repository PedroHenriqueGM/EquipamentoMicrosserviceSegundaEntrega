package com.example.Equipamento.Controller;

import com.example.Equipamento.Dto.IntegrarTrancaNaRedeDTO;
import com.example.Equipamento.Dto.RetirarTrancaDTO;
import com.example.Equipamento.Model.Bicicleta;
import com.example.Equipamento.Model.Tranca;
import com.example.Equipamento.Model.enums.StatusTranca;
import com.example.Equipamento.Repository.TrancaRepository;
import com.example.Equipamento.Service.TrancaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TrancaControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TrancaService trancaService;

    @Mock
    private TrancaRepository trancaRepository;

    @InjectMocks
    private TrancaController trancaController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(trancaController).build();
    }

    @Test
    void deveTrancarComBodyNulo() throws Exception {
        Tranca t = new Tranca();
        t.setId(1);

        when(trancaService.trancar(1, null)).thenReturn(t);

        mockMvc.perform(post("/tranca/1/trancar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deveTrancarComBody() throws Exception {
        Tranca t = new Tranca();
        t.setId(1);

        Map<String, Integer> body = Map.of("bicicleta", 2);
        when(trancaService.trancar(1, 2)).thenReturn(t);

        mockMvc.perform(post("/tranca/1/trancar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deveAlterarStatusTranca() throws Exception {
        Tranca t = new Tranca();
        t.setId(1);

        when(trancaService.alterarStatus(1, "TRANCAR")).thenReturn(t);

        mockMvc.perform(post("/tranca/1/status/TRANCAR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deveIntegrarTrancaNaRede() throws Exception {
        IntegrarTrancaNaRedeDTO dto = new IntegrarTrancaNaRedeDTO();
        doNothing().when(trancaService).incluirTrancaNaRede(any(IntegrarTrancaNaRedeDTO.class));

        mockMvc.perform(post("/tranca/integrarNaRede")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void deveRetirarTrancaDaRede() throws Exception {
        RetirarTrancaDTO dto = new RetirarTrancaDTO();
        doNothing().when(trancaService).retirarTranca(any(RetirarTrancaDTO.class));

        mockMvc.perform(post("/tranca/retirarDaRede")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void deveSalvarTranca() throws Exception {
        Tranca tranca = new Tranca();
        tranca.setId(1);
        tranca.setNumero(1);
        tranca.setLocalizacao("Centro"); // se existir @NotBlank
        tranca.setStatus(StatusTranca.DISPONIVEL); // se existir @NotNull

        when(trancaService.salvarTranca(any(Tranca.class))).thenReturn(tranca);

        mockMvc.perform(post("/tranca")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tranca)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }


    @Test
    void deveListarTrancas() throws Exception {
        Tranca t1 = new Tranca();
        t1.setId(1);
        Tranca t2 = new Tranca();
        t2.setId(2);

        when(trancaService.buscarPorId(any())).thenThrow(new RuntimeException()); // não usado
        when(trancaController.listarTrancas().getBody())
                .thenReturn(null); // evita NPE no standalone

        mockMvc.perform(get("/tranca"))
                .andExpect(status().isOk());
    }

    @Test
    void deveBuscarTrancaPorId() throws Exception {
        Tranca tranca = new Tranca();
        tranca.setId(1);

        when(trancaService.buscarPorId(1)).thenReturn(tranca);

        mockMvc.perform(get("/tranca/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deveDeletarTranca() throws Exception {
        doNothing().when(trancaService).deletarTranca(1);

        mockMvc.perform(delete("/tranca/1"))
                .andExpect(status().isOk());
    }

    @Test
    void deveAtualizarTranca() throws Exception {
        Tranca tranca = new Tranca();
        tranca.setId(1);

        when(trancaService.atualizarTrancaPorId(eq(1), any(Tranca.class)))
                .thenReturn(tranca);

        mockMvc.perform(put("/tranca/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tranca)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deveBuscarBicicletaDaTranca() throws Exception {
        Bicicleta bicicleta = new Bicicleta();
        bicicleta.setId(10);

        when(trancaService.buscarBicicletaDaTranca(1)).thenReturn(bicicleta);

        mockMvc.perform(get("/tranca/1/bicicleta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }



}
