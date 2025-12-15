package com.example.Equipamento.Service;

import com.example.Equipamento.Dto.FuncionarioDTO;
import com.example.Equipamento.Dto.IncluirBicicletaDTO;
import com.example.Equipamento.Dto.RetirarBicicletaDTO;
import com.example.Equipamento.Model.Bicicleta;
import com.example.Equipamento.Model.Totem;
import com.example.Equipamento.Model.Tranca;
import com.example.Equipamento.Model.enums.StatusBicicleta;
import com.example.Equipamento.Model.enums.StatusTranca;
import com.example.Equipamento.Repository.BicicletaRepository;
import com.example.Equipamento.Repository.TrancaRepository;
import com.example.Equipamento.Repository.TotemRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects; 
import java.util.stream.Collectors; 

@Service
public class BicicletaService {
    private final BicicletaRepository repository;
    private final TrancaRepository trancaRepository;
    private final TotemRepository totemRepository;
    private final EmailService emailService;
    private final IntegracaoService integracaoService;

    public BicicletaService(BicicletaRepository repository, TrancaRepository trancaRepository, TotemRepository totemRepository, EmailService emailService, IntegracaoService integracaoService) {
        this.repository = repository;
        this.trancaRepository = trancaRepository;
        this.totemRepository = totemRepository;
        this.emailService = emailService;
        this.integracaoService = integracaoService;
    }

    private static final String MSG_BICICLETA_NAO_ENCONTRADA = "Bicicleta não encontrada";

    public Bicicleta incluirBicicleta(Bicicleta bicicleta) {

        // R2 – Campos obrigatórios para cadastrar bicicleta
        if (bicicleta.getMarca() == null || bicicleta.getMarca().isBlank() ||
                bicicleta.getModelo() == null || bicicleta.getModelo().isBlank() ||
                bicicleta.getAno() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "R2: Marca, modelo e ano são obrigatórios para cadastrar uma bicicleta."
            );
        }

        // R1: status inicial "nova"
        bicicleta.setStatus(StatusBicicleta.NOVA);

        // Primeiro salva para gerar o ID
        Bicicleta salva = repository.saveAndFlush(bicicleta);

        // R5: usa o próprio ID como número gerado pelo sistema
        salva.setNumero("BIC-" + salva.getId());

        // Atualiza o registro já com número
        repository.saveAndFlush(salva);
        return salva;
    }

    //private final TrancaRepository trancaRepository; // injete via construtor

    public void deletarBicicleta(Integer id) {
        Bicicleta b = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MSG_BICICLETA_NAO_ENCONTRADA));

        if (b.getStatus() != StatusBicicleta.APOSENTADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "R4: apenas bicicletas 'APOSENTADA' podem ser excluídas");
        }

        if (trancaRepository.existsByBicicletaId(b.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "R4: bicicleta vinculada a uma tranca não pode ser excluída");
        }

        b.setStatus(StatusBicicleta.EXCLUIDA);
        repository.saveAndFlush(b);
    }


    public Bicicleta buscarPorId(Integer id) {
        return repository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, MSG_BICICLETA_NAO_ENCONTRADA));
    }


    public Bicicleta atualizarBicicletaPorId(Integer id, Bicicleta req) {
        Bicicleta entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MSG_BICICLETA_NAO_ENCONTRADA));

        if (req.getMarca() == null || req.getMarca().isBlank()
                || req.getModelo() == null || req.getModelo().isBlank()
                || req.getAno() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "R2: Marca, modelo e ano são obrigatórios.");
        }

        if (req.getNumero() != null && !req.getNumero().equals(entity.getNumero())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "R3: o número não pode ser alterado.");
        }

        if (req.getStatus() != null && req.getStatus() != entity.getStatus()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "R1: o status da bicicleta não pode ser alterado via PUT.");
        }

        entity.setMarca(req.getMarca());
        entity.setModelo(req.getModelo());
        entity.setAno(req.getAno());
        entity.setLocalizacao(req.getLocalizacao());

        repository.saveAndFlush(entity);
        return entity;
    }

    public List<Bicicleta> listarBicicletasDoTotem(Long idTotem) {
        Totem totem = totemRepository.findById(idTotem)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Totem não encontrado."));

        return totem.getTrancas().stream()
                .map(Tranca::getBicicleta)
                .filter(Objects::nonNull)
                .map(bicicleta -> Bicicleta.builder()
                        .id(bicicleta.getId())
                        .numero(bicicleta.getNumero())
                        .status(bicicleta.getStatus())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void incluirBicicletaNaRede(IncluirBicicletaDTO dto) {

        // 1. Buscar bicicleta
        Bicicleta bicicleta = repository.findById(Math.toIntExact(dto.getIdBicicleta()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Bicicleta não encontrada."
                ));

        // 2. Validar funcionário (reparador)
        FuncionarioDTO funcionario;
        try {
            funcionario = integracaoService.buscarFuncionario(dto.getIdReparador());
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Reparador informado não existe."
            );
        }

        // 3. Validar status da bicicleta
        if (bicicleta.getStatus() != StatusBicicleta.NOVA &&
                bicicleta.getStatus() != StatusBicicleta.EM_REPARO) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Bicicleta deve estar com status NOVA ou EM_REPARO."
            );
        }

        // 4. Se estiver EM_REPARO, validar reparador
        if (bicicleta.getStatus() == StatusBicicleta.EM_REPARO) {

            if (bicicleta.getReparador() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Bicicleta em reparo não possui reparador associado."
                );
            }

            if (!bicicleta.getReparador().equals(dto.getIdReparador())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "O reparador que está devolvendo a bicicleta não é o mesmo que retirou para reparo."
                );
            }
        }

        // 5. Buscar tranca
        Tranca tranca = trancaRepository.findById(Math.toIntExact(dto.getIdTranca()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Tranca não encontrada."
                ));

        // 6. Tranca deve estar LIVRE
        if (tranca.getStatus() != StatusTranca.LIVRE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A tranca deve estar LIVRE para receber bicicleta."
            );
        }

        // 7. Atualizar estado
        bicicleta.setStatus(StatusBicicleta.DISPONIVEL);
        bicicleta.setReparador(null); // limpa reparador ao voltar para rede
        tranca.setBicicleta(bicicleta);

        repository.saveAndFlush(bicicleta);
        trancaRepository.saveAndFlush(tranca);

    }

    @Transactional
    public void retirarBicicleta(RetirarBicicletaDTO dto) {

        // 1. Buscar tranca
        Tranca tranca = trancaRepository.findById(Math.toIntExact(dto.getIdTranca()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Número da tranca inválido."
                ));

        // 2. Garantir bicicleta na tranca
        Bicicleta bicicleta = tranca.getBicicleta();
        if (bicicleta == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não há bicicleta presa nesta tranca."
            );
        }

        // 3. Validar bicicleta informada
        if (dto.getIdBicicleta() != null &&
                !dto.getIdBicicleta().equals((long) bicicleta.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A bicicleta informada não corresponde à bicicleta presa na tranca."
            );
        }

        // 4. Validar status atual
        if (bicicleta.getStatus() != StatusBicicleta.REPARO_SOLICITADO) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A bicicleta deve estar com status REPARO_SOLICITADO para retirada."
            );
        }

        // 5. Validar destino
        String statusDestino = dto.getStatusAcaoReparador() != null
                ? dto.getStatusAcaoReparador().toUpperCase()
                : "";

        if (!statusDestino.equals("EM_REPARO") && !statusDestino.equals("APOSENTADA")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "statusAcaoReparador deve ser 'EM_REPARO' ou 'APOSENTADA'."
            );
        }

        // 6. Aplicar status final + reparador
        if (statusDestino.equals("EM_REPARO")) {
            bicicleta.setStatus(StatusBicicleta.EM_REPARO);
            bicicleta.setReparador(dto.getIdFuncionario());
        } else {
            bicicleta.setStatus(StatusBicicleta.APOSENTADA);
            bicicleta.setReparador(null);
        }

        // 7. Liberar tranca
        tranca.setBicicleta(null);
        tranca.setStatus(StatusTranca.LIVRE);

        // 8. Persistir
        repository.saveAndFlush(bicicleta);
        trancaRepository.saveAndFlush(tranca);
    }



    public Bicicleta alterarStatus(Integer idBicicleta, String acaoRaw) {
        Bicicleta bicicleta = repository.findById(idBicicleta)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        MSG_BICICLETA_NAO_ENCONTRADA
                ));
        if (acaoRaw == null || acaoRaw.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Ação não informada."
            );
        }
        StatusBicicleta novoStatus;
        try {
            novoStatus = StatusBicicleta.valueOf(acaoRaw.toUpperCase());
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Status inválido. Use DISPONIVEL, EM_USO, NOVA, APOSENTADA, REPARO_SOLICITADO, EM_REPARO."
            );
        }
        bicicleta.setStatus(novoStatus);
        return repository.saveAndFlush(bicicleta);
    }



}

