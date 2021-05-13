package br.com.pedrodavi.financesapi.controller;

import br.com.pedrodavi.financesapi.exception.AuthError;
import br.com.pedrodavi.financesapi.exception.BusinessRuleException;
import br.com.pedrodavi.financesapi.model.User;
import br.com.pedrodavi.financesapi.model.dto.UserAuthDTO;
import br.com.pedrodavi.financesapi.model.dto.UserDTO;
import br.com.pedrodavi.financesapi.service.LaunchService;
import br.com.pedrodavi.financesapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@CrossOrigin("https://finances.pedrodavi.com.br")
public class UserController {

    private final UserService service;
    private final LaunchService lancamentoService;

    @PostMapping("/autenticar")
    public ResponseEntity autenticar(@RequestBody UserAuthDTO dto ) {
        try {
            User usuarioAutenticado = service.autenticar(dto.getEmail(), dto.getSenha());
            return ResponseEntity.ok(usuarioAutenticado);
        }catch (AuthError e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity salvar( @RequestBody UserDTO dto ) {

        User usuario = User.builder()
                .nome(dto.getNome())
                .email(dto.getEmail())
                .senha(dto.getSenha()).build();

        try {
            User usuarioSalvo = service.salvarUsuario(usuario);
            return new ResponseEntity(usuarioSalvo, HttpStatus.CREATED);
        }catch (BusinessRuleException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @GetMapping("{id}/saldo")
    public ResponseEntity obterSaldo( @PathVariable("id") Long id ) {
        Optional<User> usuario = service.obterPorId(id);

        if(usuario.isEmpty()) {
            return new ResponseEntity( HttpStatus.NOT_FOUND );
        }

        BigDecimal saldo = lancamentoService.obterSaldoPorUsuario(id);
        return ResponseEntity.ok(saldo);
    }

}
