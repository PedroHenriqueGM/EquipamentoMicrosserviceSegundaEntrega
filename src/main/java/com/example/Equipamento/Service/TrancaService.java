package com.example.Equipamento.Service;

import com.example.Equipamento.Dto.FuncionarioDTO;
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
    private final IntegracaoService integracaoService;


    public TrancaService(TrancaRepository repository, BicicletaRepository bicicletaRepository, TotemRepository totemRepository, EmailService emailService, IntegracaoService integracaoService) {
        this.repository = repository;
        this.bicicletaRepository = bicicletaRepository;
        this.totemRepository = totemRepository;
        this.emailService = emailService;
        this.integracaoService = integracaoService;
    }

    private static final String MSG_TRANCA_NAO_ENCONTRADA = "Tranca não encontrada";

    public Tranca salvarTranca(Tranca tranca) {

        // R2 – Campos obrigatórios para cadastrar tranca
        if (tranca.getModelo() == null || tranca.getModelo().isBlank() ||
                tranca.getAno() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "R2: Modelo e ano são obrigatórios para cadastrar uma tranca."
            );
        }
        // R1: status inicial "nova"
        tranca.setStatus(StatusTranca.NOVA);

        // Primeiro salva para gerar o ID
        Tranca salva = repository.saveAndFlush(tranca);

        // R5: usa o próprio ID como número gerado pelo sistema
        salva.setNumero("TR-" + salva.getId());

        // Atualiza o registro já com número
        repository.saveAndFlush(salva);
        return salva;
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
        tranca.setStatus(StatusTranca.EXCLUIDA);
        repository.saveAndFlush(tranca);
    }

    public Tranca atualizarTrancaPorId(Integer id, Tranca req) {
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
        if (req.getStatus() != null &&
                req.getStatus() != entity.getStatus()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "R1: o status da tranca não pode ser alterado via atualização.");
        }

        // Atualização completa
        entity.setModelo(req.getModelo());
        entity.setAno(req.getAno());
        entity.setLocalizacao(req.getLocalizacao());

        repository.saveAndFlush(entity);
        return entity;
    }

    @Transactional
    public Tranca trancar(Integer idTranca, Integer idBicicleta) {

        Tranca tranca = repository.findById(idTranca)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        MSG_TRANCA_NAO_ENCONTRADA
                ));

        // Só pode TRANCAR se estiver LIVRE
        if (tranca.getStatus() != StatusTranca.LIVRE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A tranca só pode ser TRANCADA quando está LIVRE."
            );
        }

        Bicicleta bike = null;

        if (idBicicleta != null) {
            // Busca bicicleta pelo ID (não mais pelo número)
            bike = bicicletaRepository.findById(idBicicleta)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Bicicleta não encontrada."
                    ));

            // Verifica se já existe alguma tranca usando essa bicicleta
            if (repository.existsByBicicletaId(idBicicleta)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Esta bicicleta já está presa em outra tranca."
                );
            }

            // Vincula bicicleta à tranca
            tranca.setBicicleta(bike);

            // opcional: atualizar status da bicicleta
            bike.setStatus(StatusBicicleta.DISPONIVEL);
            bicicletaRepository.saveAndFlush(bike);
        }

        // Atualiza status da tranca
        tranca.setStatus(StatusTranca.OCUPADA);
        repository.saveAndFlush(tranca);

        return tranca;
    }


    @Transactional
    public Tranca destrancar(Integer idTranca, Integer idBicicleta) {

        Tranca tranca = repository.findById(idTranca)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        MSG_TRANCA_NAO_ENCONTRADA
                ));

        // Só pode destrancar quando está OCUPADA
        if (tranca.getStatus() != StatusTranca.OCUPADA) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A tranca só pode ser DESTRANCADA quando está OCUPADA."
            );
        }

        Bicicleta bike = tranca.getBicicleta();

        if (bike == null) {
            if (idBicicleta != null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Nenhuma bicicleta está presa nesta tranca."
                );
            }
        } else {
            if (idBicicleta != null && bike.getId() != idBicicleta.intValue()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "A bicicleta informada não corresponde à bicicleta presa na tranca."
                );
            }

            // bicicleta sai da tranca → disponível
            bike.setStatus(StatusBicicleta.DISPONIVEL);
            bicicletaRepository.saveAndFlush(bike);

            tranca.setBicicleta(null);
        }

        tranca.setStatus(StatusTranca.LIVRE);
        repository.saveAndFlush(tranca);

        return tranca;
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

        // 3. Validar funcionário (reparador)
        FuncionarioDTO funcionario;
        try {
            funcionario = integracaoService.buscarFuncionario(dto.getIdReparador());
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Reparador informado não existe."
            );
        }

        // 4. Validar status: deve ser "nova" ou "em_reparo"
        if (tranca.getStatus() != StatusTranca.NOVA &&
                tranca.getStatus() != StatusTranca.EM_REPARO) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A tranca deve estar com status NOVA ou EM_REPARO."
            );
        }

        // 5. R3 – Em reparo, verificar se funcionario é o mesmo
        if (tranca.getStatus() == StatusTranca.EM_REPARO) {

            if (tranca.getReparador() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Tranca em reparo não possui reparador associado."
                );
            }

            if (!tranca.getReparador().equals(dto.getIdReparador())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "O reparador que está devolvendo a tranca não é o mesmo que retirou para reparo."
                );
            }
        }

        // 6. Vincular tranca ao totem
        tranca.setTotem(totem);

        if (totem.getTrancas() != null && !totem.getTrancas().contains(tranca)) {
            totem.getTrancas().add(tranca);
        }

        // 7. Alterar status para "disponível" → "livre"
        tranca.setStatus(StatusTranca.LIVRE);

        // 8. Persistir
        repository.saveAndFlush(tranca);
        totemRepository.saveAndFlush(totem);

    }
    @Transactional
    public void retirarTranca(RetirarTrancaDTO dto) {

        // 1. Buscar tranca
        Tranca tranca = repository.findById(Math.toIntExact(dto.getIdTranca()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Número da tranca inválido."
                ));

        // 2. (Opcional) validar se o totem informado bate com o da tranca
        if (dto.getIdTotem() != null) {
            Totem totemAtual = tranca.getTotem();
            if (totemAtual == null || !dto.getIdTotem().equals(totemAtual.getId().longValue())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "A tranca não pertence ao totem informado."
                );
            }
        }

        // 3. Validar status “reparo solicitado”
        if (tranca.getStatus() != StatusTranca.REPARO_SOLICITADO) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A tranca deve estar com status REPARO_SOLICITADO para retirada."
            );
        }

        // 4. Definir destino a partir de statusAcaoReparador
        String statusDestino = dto.getStatusAcaoReparador() != null
                ? dto.getStatusAcaoReparador().toUpperCase()
                : "";

        if (!statusDestino.equals("EM_REPARO") && !statusDestino.equals("APOSENTADA")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "statusAcaoReparador deve ser 'EM_REPARO' ou 'APOSENTADA'."
            );
        }

        // 5. Desassociar do totem
        tranca.setTotem(null);

        // 6. Alterar status final
        if (statusDestino.equals("EM_REPARO")) {
            tranca.setStatus(StatusTranca.EM_REPARO);
            tranca.setReparador(dto.getIdFuncionario());
        } else { // APOSENTADA
            tranca.setStatus(StatusTranca.APOSENTADA);
            tranca.setReparador(null);
        }

        // 8. Persistir
        repository.saveAndFlush(tranca);
    }

    public Tranca alterarStatus(Integer idTranca, String acaoRaw) {

        Tranca tranca = repository.findById(idTranca)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, MSG_TRANCA_NAO_ENCONTRADA));

        if (acaoRaw == null || acaoRaw.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Ação não informada.");
        }

        String acao = acaoRaw.toUpperCase();

        switch (acao) {

            case "TRANCAR":
                if (tranca.getStatus() != StatusTranca.LIVRE) {
                    throw new ResponseStatusException(
                            HttpStatus.UNPROCESSABLE_ENTITY,
                            "Só é possível TRANCAR quando a tranca está LIVRE.");
                }
                tranca.setStatus(StatusTranca.OCUPADA);
                break;

            case "DESTRANCAR":
                if (tranca.getStatus() != StatusTranca.OCUPADA) {
                    throw new ResponseStatusException(
                            HttpStatus.UNPROCESSABLE_ENTITY,
                            "Só é possível DESTRANCAR quando a tranca está OCUPADA.");
                }
                tranca.setStatus(StatusTranca.LIVRE);
                break;

            case "REPARO_SOLICITADO":
                tranca.setStatus(StatusTranca.REPARO_SOLICITADO);
                break;
            case "APOSENTADA":
                tranca.setStatus(StatusTranca.APOSENTADA);
                break;

            default:
                throw new ResponseStatusException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "Ação inválida. Use TRANCAR ou DESTRANCAR."
                );
        }

        return repository.saveAndFlush(tranca);
    }

    public Tranca buscarPorId(Integer idTranca) {
        return repository.findById(idTranca)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        MSG_TRANCA_NAO_ENCONTRADA
                ));
    }

    public Bicicleta buscarBicicletaDaTranca(Integer idTranca) {
        Tranca tranca = repository.findById(idTranca)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        MSG_TRANCA_NAO_ENCONTRADA
                ));

        Bicicleta bicicleta = tranca.getBicicleta();
        if (bicicleta == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Nenhuma bicicleta está associada a esta tranca."
            );
        }

        return bicicleta;
    }

    public List<Tranca> listarTrancasDoTotem(Long idTotem) {
        if (!totemRepository.existsById(idTotem)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Totem não encontrado.");
        }

        return repository.findByTotemId(idTotem);
    }
}
