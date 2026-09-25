package com.example.learnmaster.tccv2.controller;

import com.example.learnmaster.tccv2.dto.LembreteRequest;
import com.example.learnmaster.tccv2.dto.LembreteResponse;
import com.example.learnmaster.tccv2.exception.ApiException;
import com.example.learnmaster.tccv2.model.Lembrete;
import com.example.learnmaster.tccv2.repository.DeckRepository;
import com.example.learnmaster.tccv2.repository.LembreteRepository;
import com.example.learnmaster.tccv2.security.UsuarioLogado;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 * Lembretes de estudo (app mobile). Ficam no banco para aparecerem em qualquer aparelho;
 * cada aparelho agenda as notificacoes localmente a partir desta lista.
 * Cada usuario so ve e altera os proprios lembretes.
 */
@RestController
@RequestMapping("/lembretes")
public class LembreteController {

    private static final int MAX_LEMBRETES = 20;

    private final LembreteRepository lembreteRepository;
    private final DeckRepository deckRepository;

    public LembreteController(LembreteRepository lembreteRepository, DeckRepository deckRepository) {
        this.lembreteRepository = lembreteRepository;
        this.deckRepository = deckRepository;
    }

    @GetMapping
    public List<LembreteResponse> listar(@AuthenticationPrincipal Jwt jwt) {
        return lembreteRepository.findByUsuarioIdOrderByHorarioAscIdAsc(UsuarioLogado.id(jwt)).stream()
                .map(LembreteResponse::of)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LembreteResponse criar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody LembreteRequest req) {
        Integer usuarioId = UsuarioLogado.id(jwt);
        if (lembreteRepository.countByUsuarioId(usuarioId) >= MAX_LEMBRETES) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Você pode ter até " + MAX_LEMBRETES + " lembretes. Exclua algum para criar outro.");
        }
        Lembrete lembrete = new Lembrete();
        lembrete.setUsuarioId(usuarioId);
        preencher(lembrete, req, usuarioId);
        return LembreteResponse.of(lembreteRepository.save(lembrete));
    }

    @PutMapping("/{id}")
    public LembreteResponse atualizar(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer id,
                                      @Valid @RequestBody LembreteRequest req) {
        Integer usuarioId = UsuarioLogado.id(jwt);
        Lembrete lembrete = doUsuario(id, usuarioId);
        preencher(lembrete, req, usuarioId);
        return LembreteResponse.of(lembreteRepository.save(lembrete));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer id) {
        lembreteRepository.delete(doUsuario(id, UsuarioLogado.id(jwt)));
    }

    private Lembrete doUsuario(Integer id, Integer usuarioId) {
        return lembreteRepository.findByIdAndUsuarioId(id, usuarioId).orElseThrow(ApiException::naoEncontrado);
    }

    // Valida as regras que dependem da frequencia e copia os campos para a entidade
    private void preencher(Lembrete lembrete, LembreteRequest req, Integer usuarioId) {
        if (req.deckId() != null) {
            deckRepository.doUsuario(req.deckId(), usuarioId).orElseThrow(ApiException::naoEncontrado);
        }

        int dias = 0;
        switch (req.frequencia()) {
            case UMA_VEZ -> {
                if (req.data() == null) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "Escolha a data do lembrete.");
                }
            }
            case SEMANAL -> {
                if (req.diasSemana() == null || req.diasSemana().isEmpty()) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "Escolha pelo menos um dia da semana.");
                }
                for (Integer dia : req.diasSemana()) dias |= 1 << (dia - 1);
            }
            case DIARIO -> {
            }
        }

        lembrete.setTitulo(req.titulo().trim());
        lembrete.setDeckId(req.deckId());
        lembrete.setFrequencia(req.frequencia());
        lembrete.setData(req.frequencia() == Lembrete.Frequencia.UMA_VEZ ? req.data() : null);
        lembrete.setHorario(req.horario().withSecond(0).withNano(0));
        lembrete.setDiasSemana(dias);
        if (req.ativo() != null) lembrete.setAtivo(req.ativo());
    }
}
