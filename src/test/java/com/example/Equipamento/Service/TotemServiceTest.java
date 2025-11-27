package com.example.Equipamento.Service;

import com.example.Equipamento.Dto.TotemDTO;
import com.example.Equipamento.Model.Totem;
import com.example.Equipamento.Model.Tranca;
import com.example.Equipamento.Repository.TotemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TotemServiceTest {

    @Mock
    private TotemRepository totemRepository;

    @InjectMocks
    private TotemService totemService;

    private Totem totem;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        totem = new Totem();
        totem.setId(1L);                 // o setter está vazio, mas não usamos o id nas asserções
        totem.setDescricao("Totem Central");
        totem.setLocalizacao("Praça Central");
        // por padrão, a lista de trancas vem vazia (size = 0)
    }

    @Test
    void deveListarTotensComSucesso() {
        when(totemRepository.findAll()).thenReturn(Arrays.asList(totem));

        List<TotemDTO> result = totemService.listarTotens();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).descricao()).isEqualTo("Totem Central");

        verify(totemRepository, times(1)).findAll();
    }

    @Test
    void deveExcluirTotemSemTrancas() {
        totem.setTrancas(Collections.emptyList());
        when(totemRepository.findById(1L)).thenReturn(Optional.of(totem));

        totemService.excluirTotem(1L);

        verify(totemRepository, times(1)).delete(totem);
    }

    @Test
    void deveLancarExcecao_QuandoTotemNaoEncontrado() {
        when(totemRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> totemService.excluirTotem(99L));

        assertThat(ex.getStatusCode().value()).isEqualTo(404);
        assertThat(ex.getReason()).contains("Totem não encontrado");
    }

    @Test
    void deveLancarExcecao_QuandoTotemPossuiTrancas() {
        Totem t = new Totem();
        t.setId(2L);
        Tranca tranca = new Tranca();
        t.setTrancas(List.of(tranca));

        when(totemRepository.findById(2L)).thenReturn(Optional.of(t));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> totemService.excluirTotem(2L));

        assertThat(ex.getStatusCode().value()).isEqualTo(400);
        assertThat(ex.getReason()).contains("Totem possui trancas");
    }

    @Test
    void deveAtualizarTotemComSucesso() {
        when(totemRepository.findById(1L)).thenReturn(Optional.of(totem));
        when(totemRepository.saveAndFlush(any(Totem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NovoTotemDTO dto = new NovoTotemDTO(
                "Nova Localização",
                "Descrição atualizada do totem"
        );

        TotemDTO resultado = totemService.atualizarTotem(1L, dto);

        assertThat(resultado.localizacao()).isEqualTo("Nova Localização");
        assertThat(resultado.descricao()).isEqualTo("Descrição atualizada do totem");
        // como não adicionamos trancas no setUp, o totalTrancas é 0
        assertThat(resultado.totalTrancas()).isEqualTo(0);

        verify(totemRepository, times(1)).findById(1L);
        verify(totemRepository, times(1)).saveAndFlush(any(Totem.class));
    }
}
