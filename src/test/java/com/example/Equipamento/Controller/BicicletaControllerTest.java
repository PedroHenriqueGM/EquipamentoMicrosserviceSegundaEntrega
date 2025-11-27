package com.example.Equipamento.Controller;

import com.example.Equipamento.Dto.IncluirBicicletaDTO;
import com.example.Equipamento.Dto.RetirarBicicletaDTO;
import com.example.Equipamento.Model.Bicicleta;
import com.example.Equipamento.Repository.BicicletaRepository;
import com.example.Equipamento.Service.BicicletaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BicicletaController.class)
class BicicletaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BicicletaService bicicletaService;

    @Autowired
    private BicicletaRepository bicicletaRepository;

    // ---------------- CONFIGURAÇÃO TESTE -------------------
    @TestConfiguration
    static class Config {

        @Bean
        public BicicletaService bicicletaService() {
            return Mockito.mock(BicicletaService.class);
        }

        @Bean
        public BicicletaRepository bicicletaRepository() {
            return Mockito.mock(BicicletaRepository.class);
        }
    }

    // ----------------------- TESTES -------------------------

    @Test
    void deveIncluirBicicleta() throws Exception {
        Bicicleta b = new Bicicleta();
        b.setId(1);

        Mockito.when(bicicletaService.incluirBicicleta(any(Bicicleta.class)))
                .thenReturn(b);

        mockMvc.perform(post("/bicicleta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(b)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deveListarBicicletas() throws Exception {
        Bicicleta b = new Bicicleta();
        b.setId(1);

        Mockito.when(bicicletaRepository.findAll())
                .thenReturn(List.of(b));

        mockMvc.perform(get("/bicicleta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void deveBuscarPorId() throws Exception {
        Bicicleta b = new Bicicleta();
        b.setId(1);

        Mockito.when(bicicletaService.buscarPorId(1))
                .thenReturn(b);

        mockMvc.perform(get("/bicicleta/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deveDeletarBicicleta() throws Exception {
        Mockito.doNothing().when(bicicletaService).deletarBicicleta(1);

        mockMvc.perform(delete("/bicicleta/1"))
                .andExpect(status().isOk());
    }

    @Test
    void deveAtualizarBicicleta() throws Exception {
        Bicicleta b = new Bicicleta();
        b.setId(1);

        Mockito.when(bicicletaService.atualizarBicicletaPorId(eq(1), any(Bicicleta.class)))
                .thenReturn(b);

        mockMvc.perform(put("/bicicleta/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(b)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deveIntegrarNaRede() throws Exception {
        IncluirBicicletaDTO dto = new IncluirBicicletaDTO();
        dto.setIdBicicleta(1L);
        dto.setIdTranca(2L);

        Mockito.doNothing().when(bicicletaService).incluirBicicletaNaRede(any(IncluirBicicletaDTO.class));

        mockMvc.perform(post("/bicicleta/integrarNaRede")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void deveRetirarDaRede() throws Exception {
        RetirarBicicletaDTO dto = new RetirarBicicletaDTO();
        dto.setIdBicicleta(1L);
        dto.setIdTranca(2L);

        Mockito.doNothing().when(bicicletaService).retirarBicicleta(any(RetirarBicicletaDTO.class));

        mockMvc.perform(post("/bicicleta/retirarDaRede")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void deveAlterarStatus() throws Exception {
        Bicicleta b = new Bicicleta();
        b.setId(1);

        Mockito.when(bicicletaService.alterarStatus(1, "TRANCAR"))
                .thenReturn(b);

        mockMvc.perform(post("/bicicleta/1/status/TRANCAR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}
