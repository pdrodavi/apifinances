package br.com.pedrodavi.financesapi.controller;

import br.com.pedrodavi.financesapi.enums.LaunchStatus;
import br.com.pedrodavi.financesapi.enums.ReleaseType;
import br.com.pedrodavi.financesapi.exception.BusinessRuleException;
import br.com.pedrodavi.financesapi.model.Launch;
import br.com.pedrodavi.financesapi.model.User;
import br.com.pedrodavi.financesapi.model.dto.LaunchDTO;
import br.com.pedrodavi.financesapi.model.dto.UpdateStatusDTO;
import br.com.pedrodavi.financesapi.service.LaunchService;
import br.com.pedrodavi.financesapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/lancamentos")
@RequiredArgsConstructor
@CrossOrigin("https://finances.pedrodavi.com.br")
public class LaunchController {

    private final LaunchService service;
    private final UserService usuarioService;

    @GetMapping
    public ResponseEntity buscar(
            @RequestParam(value ="descricao" , required = false) String descricao,
            @RequestParam(value = "mes", required = false) Integer mes,
            @RequestParam(value = "ano", required = false) Integer ano,
            @RequestParam("usuario") Long idUsuario
    ) {

        Launch lancamentoFiltro = new Launch();
        lancamentoFiltro.setDescricao(descricao);
        lancamentoFiltro.setMes(mes);
        lancamentoFiltro.setAno(ano);

        Optional<User> usuario = usuarioService.obterPorId(idUsuario);
        if(usuario.isEmpty()) {
            return ResponseEntity.badRequest().body("Não foi possível realizar a consulta. Usuário não encontrado para o Id informado.");
        }else {
            lancamentoFiltro.setUsuario(usuario.get());
        }

        List<Launch> lancamentos = service.buscar(lancamentoFiltro);
        return ResponseEntity.ok(lancamentos);
    }

    @GetMapping("{id}")
    public ResponseEntity obterLancamento( @PathVariable("id") Long id ) {
        return service.obterPorId(id)
                .map( lancamento -> new ResponseEntity(converter(lancamento), HttpStatus.OK) )
                .orElseGet( () -> new ResponseEntity(HttpStatus.NOT_FOUND) );
    }

    @PostMapping
    public ResponseEntity salvar( @RequestBody LaunchDTO dto ) {
        try {
            Launch entidade = converter(dto);
            entidade = service.salvar(entidade);
            return new ResponseEntity(entidade, HttpStatus.CREATED);
        }catch (BusinessRuleException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("{id}")
    public ResponseEntity atualizar( @PathVariable("id") Long id, @RequestBody LaunchDTO dto ) {
        return service.obterPorId(id).map( entity -> {
            try {
                Launch lancamento = converter(dto);
                lancamento.setId(entity.getId());
                service.atualizar(lancamento);
                return ResponseEntity.ok(lancamento);
            }catch (BusinessRuleException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }).orElseGet( () ->
                new ResponseEntity("Lancamento não encontrado na base de Dados.", HttpStatus.BAD_REQUEST) );
    }

    @PutMapping("{id}/atualiza-status")
    public ResponseEntity atualizarStatus( @PathVariable("id") Long id , @RequestBody UpdateStatusDTO dto ) {
        return service.obterPorId(id).map( entity -> {
            LaunchStatus statusSelecionado = LaunchStatus.valueOf(dto.getStatus());

            if(statusSelecionado == null) {
                return ResponseEntity.badRequest().body("Não foi possível atualizar o status do lançamento, envie um status válido.");
            }

            try {
                entity.setStatus(statusSelecionado);
                service.atualizar(entity);
                return ResponseEntity.ok(entity);
            }catch (BusinessRuleException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }

        }).orElseGet( () ->
                new ResponseEntity("Lancamento não encontrado na base de Dados.", HttpStatus.BAD_REQUEST) );
    }

    @DeleteMapping("{id}")
    public ResponseEntity deletar( @PathVariable("id") Long id ) {
        return service.obterPorId(id).map( entidade -> {
            service.deletar(entidade);
            return new ResponseEntity( HttpStatus.NO_CONTENT );
        }).orElseGet( () ->
                new ResponseEntity("Lancamento não encontrado na base de Dados.", HttpStatus.BAD_REQUEST) );
    }

    private LaunchDTO converter(Launch lancamento) {
        return LaunchDTO.builder()
                .descricao(lancamento.getDescricao())
                .valor(lancamento.getValor())
                .mes(lancamento.getMes())
                .ano(lancamento.getAno())
                .status(lancamento.getStatus().name())
                .tipo(lancamento.getTipo().name())
                .usuario(lancamento.getUsuario().getId())
                .build();

    }

    private Launch converter(LaunchDTO dto) {
        Launch lancamento = new Launch();
        lancamento.setDescricao(dto.getDescricao());
        lancamento.setAno(dto.getAno());
        lancamento.setMes(dto.getMes());
        lancamento.setValor(dto.getValor());
        lancamento.setDataCadastro(LocalDate.now());

        User usuario = usuarioService
                .obterPorId(dto.getUsuario())
                .orElseThrow( () -> new BusinessRuleException("Usuário não encontrado para o Id informado.") );

        lancamento.setUsuario(usuario);

        if(dto.getTipo() != null) {
            lancamento.setTipo(ReleaseType.valueOf(dto.getTipo()));
        }

        if(dto.getStatus() != null) {
            lancamento.setStatus(LaunchStatus.valueOf(dto.getStatus()));
        }

        return lancamento;
    }

}
