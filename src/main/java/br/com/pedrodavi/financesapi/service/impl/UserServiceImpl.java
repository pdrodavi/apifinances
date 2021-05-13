package br.com.pedrodavi.financesapi.service.impl;

import br.com.pedrodavi.financesapi.exception.AuthError;
import br.com.pedrodavi.financesapi.exception.BusinessRuleException;
import br.com.pedrodavi.financesapi.model.User;
import br.com.pedrodavi.financesapi.repository.UserRepository;
import br.com.pedrodavi.financesapi.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private UserRepository repository;

    public UserServiceImpl(UserRepository repository) {
        super();
        this.repository = repository;
    }

    @Override
    public User autenticar(String email, String senha) {
        Optional<User> usuario = repository.findByEmail(email);

        if(usuario.isEmpty()) {
            throw new AuthError("Usuário não encontrado para o email informado.");
        }

        if(!usuario.get().getSenha().equals(senha)) {
            throw new AuthError("Senha inválida.");
        }

        return usuario.get();
    }

    @Override
    @Transactional
    public User salvarUsuario(User usuario) {
        validarEmail(usuario.getEmail());
        return repository.save(usuario);
    }

    @Override
    public void validarEmail(String email) {
        boolean existe = repository.existsByEmail(email);
        if(existe) {
            throw new BusinessRuleException("Já existe um usuário cadastrado com este email.");
        }
    }

    @Override
    public Optional<User> obterPorId(Long id) {
        return repository.findById(id);
    }

}
