package com.example.Equipamento.Controller;

import com.example.Equipamento.Dto.NovoTotemDTO;
import com.example.Equipamento.Dto.TotemDTO;
import com.example.Equipamento.Model.Bicicleta;
import com.example.Equipamento.Model.Tranca;
import com.example.Equipamento.Service.BicicletaService;
import com.example.Equipamento.Service.TotemService;
import com.example.Equipamento.Service.TrancaService;
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

@WebMvcTest(TotemController.class)
class TotemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TotemService totemService;

    @Autowired
    private TrancaService trancaService;

    @Autowired
    private BicicletaService bicicletaService;

    // ---------------- CONFIGURAÇÃO ----------------
    @TestConfiguration
    static class Config {

        @Bean
        TotemService totemService() {
            return Mockito.mock(TotemService.class);
        }

        @Bean
        TrancaService trancaService() {
            return Mockito.mock(TrancaService.class);
        }

        @Bean
        BicicletaService bicicletaService() {
            return Mockito.mock(BicicletaService.class);
        }
    }

    // ---------------- TESTES ----------------

    @Test
    void deveListarTotens() throws Exception {
        TotemDTO dto = new TotemDTO(1L, "Rua A", "Totem Central", 3);

        Mockito.when(totemService.listarTotens())
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/totem"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].localizacao").value("Rua A"))
                .andExpect(jsonPath("$[0].descricao").value("Totem Central"))
                .andExpect(jsonPath("$[0].totalTrancas").value(3));
    }

    @Test
    void deveIncluirTotem() throws Exception {
        NovoTotemDTO dto = new NovoTotemDTO("Rua A", "Totem Central");
        TotemDTO salvo = new TotemDTO(1L, "Rua A", "Totem Central", 0);

        Mockito.when(totemService.incluirTotem(any(NovoTotemDTO.class)))
                .thenReturn(salvo);

        mockMvc.perform(post("/totem")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/totem/1"))
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void deveAtualizarTotem() throws Exception {
        NovoTotemDTO dto = new NovoTotemDTO("Rua B", "Novo Totem");
        TotemDTO atualizado = new TotemDTO(1L, "Rua B", "Novo Totem", 2);

        Mockito.when(totemService.atualizarTotem(eq(1L), any(NovoTotemDTO.class)))
                .thenReturn(atualizado);

        mockMvc.perform(put("/totem/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.localizacao").value("Rua B"));
    }

    @Test
    void deveExcluirTotem() throws Exception {
        Mockito.doNothing().when(totemService).excluirTotem(1L);

        mockMvc.perform(delete("/totem/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Totem excluído com sucesso."));
    }

    @Test
    void deveListarTrancasDoTotem() throws Exception {
        Tranca t = new Tranca();
        t.setId(1);

        Mockito.when(trancaService.listarTrancasDoTotem(1L))
                .thenReturn(List.of(t));

        mockMvc.perform(get("/totem/1/trancas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void deveListarBicicletasDoTotem() throws Exception {
        Bicicleta b = new Bicicleta();
        b.setId(1);

        Mockito.when(bicicletaService.listarBicicletasDoTotem(1L))
                .thenReturn(List.of(b));

        mockMvc.perform(get("/totem/1/bicicletas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }
}
