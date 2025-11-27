package com.example.Equipamento.Controller;

import com.example.Equipamento.Dto.IntegrarTrancaNaRedeDTO;
import com.example.Equipamento.Dto.RetirarTrancaDTO;
import com.example.Equipamento.Model.Bicicleta;
import com.example.Equipamento.Model.Tranca;
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

import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TrancaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TrancaService trancaService;

    @Mock
    private TrancaRepository trancaRepository;

    @InjectMocks
    private TrancaController trancaController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(trancaController).build();
        objectMapper = new ObjectMapper();
    }


    @Test
    void listarTrancas_deveRetornar200() throws Exception {
        when(trancaRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/tranca"))
                .andExpect(status().isOk());
    }

    @Test
    void buscarPorId_deveRetornar200() throws Exception {
        Tranca tranca = new Tranca();
        when(trancaService.buscarPorId(1)).thenReturn(tranca);

        mockMvc.perform(get("/tranca/1"))
                .andExpect(status().isOk());
    }

    @Test
    void deletarTranca_deveRetornar200() throws Exception {
        doNothing().when(trancaService).deletarTranca(1);

        mockMvc.perform(delete("/tranca/1"))
                .andExpect(status().isOk());
    }

    @Test
    void atualizarTrancaPorId_deveRetornar200() throws Exception {
        Tranca tranca = new Tranca();
        when(trancaService.atualizarTrancaPorId(eq(1), any())).thenReturn(tranca);

        mockMvc.perform(put("/tranca/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tranca)))
                .andExpect(status().isOk());
    }

    @Test
    void buscarBicicletaDaTranca_deveRetornar200() throws Exception {
        Bicicleta bicicleta = new Bicicleta();
        when(trancaService.buscarBicicletaDaTranca(1)).thenReturn(bicicleta);

        mockMvc.perform(get("/tranca/1/bicicleta"))
                .andExpect(status().isOk());
    }

    @Test
    void trancar_deveRetornar200() throws Exception {
        Tranca tranca = new Tranca();
        when(trancaService.trancar(eq(1), eq(1))).thenReturn(tranca);

        mockMvc.perform(post("/tranca/1/trancar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("bicicleta", 1))))
                .andExpect(status().isOk());
    }

    @Test
    void destrancar_deveRetornar200() throws Exception {
        Tranca tranca = new Tranca();
        when(trancaService.destrancar(eq(1), eq(1))).thenReturn(tranca);

        mockMvc.perform(post("/tranca/1/destrancar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("bicicleta", 1))))
                .andExpect(status().isOk());
    }

    @Test
    void integrarTranca_deveRetornar200() throws Exception {
        IntegrarTrancaNaRedeDTO dto = new IntegrarTrancaNaRedeDTO();

        mockMvc.perform(post("/tranca/integrarNaRede")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void retirarTranca_deveRetornar200() throws Exception {
        RetirarTrancaDTO dto = new RetirarTrancaDTO();

        mockMvc.perform(post("/tranca/retirarDaRede")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void alterarStatusTranca_deveRetornar200() throws Exception {
        Tranca tranca = new Tranca();
        when(trancaService.alterarStatus(eq(1), eq("manutencao"))).thenReturn(tranca);

        mockMvc.perform(post("/tranca/1/status/manutencao"))
                .andExpect(status().isOk());
    }
}
