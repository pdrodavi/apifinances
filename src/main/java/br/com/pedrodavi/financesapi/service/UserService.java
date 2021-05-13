package br.com.pedrodavi.financesapi.service;

import br.com.pedrodavi.financesapi.model.User;

import java.util.Optional;

public interface UserService {

    User autenticar(String email, String senha);

    User salvarUsuario(User usuario);

    void validarEmail(String email);

    Optional<User> obterPorId(Long id);

}
