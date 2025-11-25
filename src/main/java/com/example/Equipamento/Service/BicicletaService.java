package com.example.Equipamento.Service;

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

    public BicicletaService(BicicletaRepository repository, TrancaRepository trancaRepository, TotemRepository totemRepository, EmailService emailService) {
        this.repository = repository;
        this.trancaRepository = trancaRepository;
        this.totemRepository = totemRepository;
        this.emailService = emailService;
    }

    private static final String MSG_BICICLETA_NAO_ENCONTRADA = "Bicicleta não encontrada";

    public void incluirBicicleta(Bicicleta bicicleta) {

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


    public void atualizarBicicletaPorId(Integer id, Bicicleta req) {
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

        // 2. Validar status — deve ser nova ou em_reparo
        StatusBicicleta status = bicicleta.getStatus();
        if (status != StatusBicicleta.NOVA && status != StatusBicicleta.EM_REPARO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Bicicleta deve estar com status NOVA ou EM_REPARO.");
        }

        // 3. TODO: validar reparador no caso EM_REPARO
        if (status == StatusBicicleta.EM_REPARO) {
            System.out.println("[AVISO] TODO: validar reparador responsável (R3).");
        }

        // 4. Buscar tranca
        Tranca tranca = trancaRepository.findById(Math.toIntExact(dto.getIdTranca()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Tranca não encontrada."
                ));

        /// 5. Tranca deve estar LIVRE
        if (tranca.getStatus() != StatusTranca.LIVRE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A tranca deve estar LIVRE para receber bicicleta.");
        }

        // 6. Registrar log
        LocalDateTime agora = LocalDateTime.now();
        System.out.printf(
                "[INCLUSAO BICICLETA] dataHora=%s, idReparador=%d, idBicicleta=%d, idTranca=%d%n",
                agora, dto.getIdReparador(), bicicleta.getId(), tranca.getId()
        );

        // 7. Atualizar associações
        bicicleta.setStatus(StatusBicicleta.DISPONIVEL);
        tranca.setBicicleta(bicicleta);

        repository.saveAndFlush(bicicleta);
        trancaRepository.saveAndFlush(tranca);

        // 8. Enviar email
        try {
            String assunto = "Inclusão de Bicicleta na Rede";
            String corpo = "A bicicleta " + bicicleta.getNumero() +
                    " foi incluída na tranca " + tranca.getId() +
                    " em " + agora + ".";

            String resultado = emailService.enviarEmail(
                    "reparador" + dto.getIdReparador() + "@empresa.com",
                    assunto,
                    corpo
            );

            if (!resultado.equalsIgnoreCase("sucesso")) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Erro ao enviar o email.");
            }

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao enviar o email.");
        }
    }

    @Transactional
    public void retirarBicicleta(RetirarBicicletaDTO dto) {

        // 1. Buscar tranca (E1 – número inválido)
        Tranca tranca = trancaRepository.findById(Math.toIntExact(dto.getIdTranca()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Número da tranca inválido."
                ));
        // 2. Garantir que há bicicleta presa na tranca (pré-condição)
        Bicicleta bicicleta = tranca.getBicicleta();
        if (bicicleta == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não há bicicleta presa nesta tranca."
            );
        }
        StatusBicicleta statusBike = bicicleta.getStatus();
        // 3. Bicicleta precisa estar REPARO_SOLICITADO
        if (statusBike != StatusBicicleta.REPARO_SOLICITADO) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A bicicleta deve estar com status REPARO_SOLICITADO para retirada."
            );
        }
        // E a tranca não pode estar LIVRE
        if (tranca.getStatus() == StatusTranca.LIVRE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A tranca não pode estar LIVRE."
            );
        }
        // 4. Validar motivo (fluxo principal x alternativo A1)
        String motivo = dto.getMotivo() != null ? dto.getMotivo().toLowerCase() : "";
        if (!motivo.equals("reparo") && !motivo.equals("aposentadoria")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Motivo deve ser 'reparo' ou 'aposentadoria'."
            );
        }

        // 5. Definir status final
        if (motivo.equals("reparo")) {
            bicicleta.setStatus(StatusBicicleta.EM_REPARO);
        } else {
            bicicleta.setStatus(StatusBicicleta.APOSENTADA);
        }

        // Remover vínculo
        tranca.setBicicleta(null);
        tranca.setStatus(StatusTranca.LIVRE);

        // 6. Registrar retirada (R1)
        LocalDateTime agora = LocalDateTime.now();
        System.out.printf(
                "[RETIRADA BICICLETA] dataHora=%s, idReparador=%d, idBicicleta=%d, idTranca=%d, motivo=%s%n",
                agora,
                dto.getIdReparador(),
                bicicleta.getId(),
                tranca.getId(),
                motivo
        );

        // 7. Persistir alterações
        repository.saveAndFlush(bicicleta);
        trancaRepository.saveAndFlush(tranca);

        // 8. Enviar email (R2) – com tratamento de erro [E2]
        try {
            String assunto = "Retirada de Bicicleta da Rede";
            String corpo = String.format(
                    "A bicicleta %s (ID=%d) foi retirada da tranca %s (ID=%d) pelo reparador %d às %s. Motivo: %s.",
                    bicicleta.getNumero(),
                    bicicleta.getId(),
                    tranca.getNumero(),
                    tranca.getId(),
                    dto.getIdReparador(),
                    agora.toString(),
                    motivo
            );
            String resultado = emailService.enviarEmail(
                    "reparador@example.com",
                    assunto,
                    corpo
            );
            if (!"sucesso".equalsIgnoreCase(resultado)) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "E2 – Não foi possível enviar o email."
                );
            }
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "E2 – Não foi possível enviar o email."
            );
        }
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

