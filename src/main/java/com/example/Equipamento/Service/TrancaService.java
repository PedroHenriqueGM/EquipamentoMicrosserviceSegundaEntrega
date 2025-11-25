package com.example.Equipamento.Service;

import com.example.Equipamento.Dto.IntegrarTrancaNaRedeDTO;
import com.example.Equipamento.Dto.RetirarTrancaDTO;
import com.example.Equipamento.Model.Bicicleta;
import com.example.Equipamento.Model.Totem;
import com.example.Equipamento.Model.Tranca;
import com.example.Equipamento.Repository.BicicletaRepository;
import com.example.Equipamento.Repository.TrancaRepository;
import com.example.Equipamento.Repository.TotemRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TrancaService {
    private final TrancaRepository repository;
    private final BicicletaRepository bicicletaRepository;
    private final TotemRepository totemRepository;
    private EmailService emailService;


    public TrancaService(TrancaRepository repository, BicicletaRepository bicicletaRepository, TotemRepository totemRepository, EmailService emailService) {
        this.repository = repository;
        this.bicicletaRepository = bicicletaRepository;
        this.totemRepository = totemRepository;
        this.emailService = emailService;
    }

    private static final String MSG_TRANCA_NAO_ENCONTRADA = "Tranca não encontrada";

    public void salvarTranca(Tranca tranca) {

        // R2 – Campos obrigatórios para cadastrar tranca
        if (tranca.getModelo() == null || tranca.getModelo().isBlank() ||
                tranca.getAno() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "R2: Modelo e ano são obrigatórios para cadastrar uma tranca."
            );
        }
        // R1: status inicial "nova"
        tranca.setStatus("nova");

        // Primeiro salva para gerar o ID
        Tranca salva = repository.saveAndFlush(tranca);

        // R5: usa o próprio ID como número gerado pelo sistema
        salva.setNumero("TR-" + salva.getId());

        // Atualiza o registro já com número
        repository.saveAndFlush(salva);
    }

    public void deletarTranca(Integer id) {
        Tranca tranca = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        MSG_TRANCA_NAO_ENCONTRADA
                ));

        // R4 – Apenas trancas sem bicicleta podem ser excluídas
        if (tranca.getBicicleta() != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "R4: tranca com bicicleta associada não pode ser excluída."
            );
        }

        // Soft delete — altera apenas o status
        tranca.setStatus("excluida");
        repository.saveAndFlush(tranca);
    }

    public void atualizarTrancaPorId(Integer id, Tranca req) {
        Tranca entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, MSG_TRANCA_NAO_ENCONTRADA
                ));

        // R2 – Todos os campos obrigatórios devem estar preenchidos
        if (req.getModelo() == null || req.getModelo().isBlank() ||
                req.getAno() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "R2: Modelo e ano são obrigatórios para atualizar uma tranca."
            );
        }

        // R3 – número não pode ser alterado
        if (req.getNumero() != null && !req.getNumero().equals(entity.getNumero())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "R3: o número da tranca não pode ser alterado.");
        }

        // R1 – status inicial 'nova' não é editável
        if (req.getStatus() != null && !req.getStatus().equalsIgnoreCase(entity.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "R1: o status da tranca não pode ser alterado via atualização."
            );
        }

        // Atualização completa
        entity.setModelo(req.getModelo());
        entity.setAno(req.getAno());
        entity.setLocalizacao(req.getLocalizacao());

        repository.saveAndFlush(entity);
    }

    public void trancar(Integer idTranca, String idBicicleta) {
        Tranca tranca = repository.findById(idTranca)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MSG_TRANCA_NAO_ENCONTRADA));

        if ("ocupada".equalsIgnoreCase(tranca.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tranca já está ocupada");
        }

        Bicicleta bike = null;
        if (idBicicleta != null) {
            bike = bicicletaRepository.findByNumero(idBicicleta)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bicicleta não encontrada"));

            // opcional: se já estiver em outra tranca, barre
            if (repository.existsByBicicletaId(bike.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Esta bicicleta já está presa em outra tranca");
            }

            // status da bicicleta ao trancar
            bike.setStatus("travada");
            bicicletaRepository.saveAndFlush(bike);

            // faz o vínculo
            tranca.setBicicleta(bike);
        }

        // status da tranca ao trancar
        tranca.setStatus("ocupada");
        repository.saveAndFlush(tranca);
    }

    public void destrancar(Integer idTranca) {
        Tranca tranca = repository.findById(idTranca)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MSG_TRANCA_NAO_ENCONTRADA));

        if ("livre".equalsIgnoreCase(tranca.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tranca já está livre");
        }

        // se houver bicicleta, atualiza status e remove vínculo
        Bicicleta bike = tranca.getBicicleta();
        if (bike != null) {
            bike.setStatus("disponivel"); // ou “nova”/“em_uso” conforme seu fluxo
            bicicletaRepository.saveAndFlush(bike);
            tranca.setBicicleta(null);
        }

        tranca.setStatus("livre");
        repository.saveAndFlush(tranca);
    }

    public List<Tranca> listarTrancasDoTotem(Long idTotem) {
        if (!totemRepository.existsById(idTotem)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Totem não encontrado.");
        }

        return repository.findByTotemId(idTotem);
    }

    @Transactional
    public void incluirTrancaNaRede(IntegrarTrancaNaRedeDTO dto) {

        // 1. Validar Totem
        Totem totem = totemRepository.findById(dto.getIdTotem())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Totem não encontrado."
                ));

        // 2. Buscar tranca pelo ID [E1]
        Tranca tranca = repository.findById(dto.getIdTranca())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Tranca não encontrada (id incorreto ou não cadastrada)."
                ));

        // 3. Validar status: deve ser "nova" ou "em_reparo"
        String status = tranca.getStatus() != null ? tranca.getStatus().toLowerCase() : "";

        if (!status.equals("nova") && !status.equals("em_reparo")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A tranca deve estar com status 'nova' ou 'em_reparo' para ser integrada na rede."
            );
        }

        // 4. R3 – Em reparo, verificar se funcionario é o mesmo
        // (Seu modelo ainda não contém esse campo — deixo como TODO)
        if (status.equals("em_reparo")) {
            System.out.println("[AVISO] TODO: validar funcionario responsável (R3).");
        }

        // 5. Registrar inclusão (R1)
        LocalDateTime agora = LocalDateTime.now();
        System.out.printf(
                "[INCLUSAO TRANCA] dataHora=%s, idFuncionario=%d, idTranca=%d, idTotem=%d%n",
                agora, dto.getIdFuncionario(), dto.getIdTranca(), dto.getIdTotem()
        );

        // 6. Vincular tranca ao totem
        tranca.setTotem(totem);

        if (totem.getTrancas() != null && !totem.getTrancas().contains(tranca)) {
            totem.getTrancas().add(tranca);
        }

        // 7. Alterar status para "disponível" → "livre"
        tranca.setStatus("livre");

        // 8. Persistir
        repository.saveAndFlush(tranca);
        totemRepository.saveAndFlush(totem);

        // 9. R2 – envio de e-mail
        try {
            String resultado = emailService.enviarEmail(
                    dto.getIdFuncionario() + "@empresa.com",      // destino fictício
                    "Inclusão de Tranca na Rede",
                    "A tranca " + dto.getIdTranca() +
                            " foi incluída no totem " + dto.getIdTotem() +
                            " em " + agora +
                            " pelo funcionário " + dto.getIdFuncionario()

            );
            System.out.println("Resultado do envio de email = " + resultado);

            if (!resultado.equalsIgnoreCase("sucesso")) {
                throw new RuntimeException("Falha no envio");
            }

        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "E2 – Não foi possível enviar o email."
            );
        }
    }
    @Transactional
    public void retirarTranca(RetirarTrancaDTO dto) {

        // 1. Buscar tranca
        Tranca tranca = repository.findById(Math.toIntExact(dto.getIdTranca()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Número da tranca inválido."
                ));

        // 2. Validar status “reparo solicitado”
        if (!"reparo_solicitado".equalsIgnoreCase(tranca.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A tranca deve estar com status 'reparo_solicitado' para retirada."
            );
        }

        // 3. Validar motivo
        String motivo = dto.getMotivo().toLowerCase();
        if (!motivo.equals("reparo") && !motivo.equals("aposentadoria")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Motivo deve ser 'reparo' ou 'aposentadoria'."
            );
        }

        // 4. Libertar tranca do totem
        tranca = repository.findById(Math.toIntExact(dto.getIdTranca()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Número da tranca inválido."
                ));

        tranca.setTotem(null);  // só ISSO já tira do totem


        // 5. Alterar status final
        if (motivo.equals("reparo")) {
            tranca.setStatus("em_reparo");
        } else {
            tranca.setStatus("aposentada");
        }

        // 6. Registrar retirada (R1)
        LocalDateTime agora = LocalDateTime.now();
        System.out.printf(
                "[RETIRADA TRANCA] dataHora=%s, idReparador=%d, idTranca=%d, motivo=%s%n",
                agora, dto.getIdReparador(), dto.getIdTranca(), motivo
        );

        // 7. Persistir
        repository.saveAndFlush(tranca);

        // 8. Enviar email (R2)
        try {
            emailService.enviarEmail(
                    "reparador@example.com",
                    "Retirada de Tranca",
                    String.format(
                            "A tranca %d foi retirada pelo reparador %d às %s. Motivo: %s.",
                            dto.getIdTranca(),
                            dto.getIdReparador(),
                            agora.toString(),
                            motivo
                    )
            );
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "E2 – Não foi possível enviar o email."
            );
        }
    }




}
