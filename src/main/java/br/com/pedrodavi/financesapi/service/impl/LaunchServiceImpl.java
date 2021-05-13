package br.com.pedrodavi.financesapi.service.impl;

import br.com.pedrodavi.financesapi.enums.LaunchStatus;
import br.com.pedrodavi.financesapi.enums.ReleaseType;
import br.com.pedrodavi.financesapi.exception.BusinessRuleException;
import br.com.pedrodavi.financesapi.model.Launch;
import br.com.pedrodavi.financesapi.repository.LaunchRepository;
import br.com.pedrodavi.financesapi.service.LaunchService;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class LaunchServiceImpl implements LaunchService {

    private LaunchRepository repository;

    public LaunchServiceImpl(LaunchRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Launch salvar(Launch lancamento) {
        validar(lancamento);
        lancamento.setStatus(LaunchStatus.PENDENTE);
        return repository.save(lancamento);
    }

    @Override
    @Transactional
    public Launch atualizar(Launch lancamento) {
        Objects.requireNonNull(lancamento.getId());
        validar(lancamento);
        return repository.save(lancamento);
    }

    @Override
    @Transactional
    public void deletar(Launch lancamento) {
        Objects.requireNonNull(lancamento.getId());
        repository.delete(lancamento);
    }

    @Override
    @Transactional
    public List<Launch> buscar(Launch lancamentoFiltro) {
        Example example = Example.of( lancamentoFiltro,
                ExampleMatcher.matching()
                        .withIgnoreCase()
                        .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING) );

        return repository.findAll(example);
    }

    @Override
    public void atualizarStatus(Launch lancamento, LaunchStatus status) {
        lancamento.setStatus(status);
        atualizar(lancamento);
    }

    @Override
    public void validar(Launch lancamento) {

        if(lancamento.getDescricao() == null || lancamento.getDescricao().trim().equals("")) {
            throw new BusinessRuleException("Informe uma Descrição válida.");
        }

        if(lancamento.getMes() == null || lancamento.getMes() < 1 || lancamento.getMes() > 12) {
            throw new BusinessRuleException("Informe um Mês válido.");
        }

        if(lancamento.getAno() == null || lancamento.getAno().toString().length() != 4 ) {
            throw new BusinessRuleException("Informe um Ano válido.");
        }

        if(lancamento.getUsuario() == null || lancamento.getUsuario().getId() == null) {
            throw new BusinessRuleException("Informe um Usuário.");
        }

        if(lancamento.getValor() == null || lancamento.getValor().compareTo(BigDecimal.ZERO) < 1 ) {
            throw new BusinessRuleException("Informe um Valor válido.");
        }

        if(lancamento.getTipo() == null) {
            throw new BusinessRuleException("Informe um tipo de Lançamento.");
        }

    }

    @Override
    public Optional<Launch> obterPorId(Long id) {
        return repository.findById(id);
    }

    @Override
    @Transactional
    public BigDecimal obterSaldoPorUsuario(Long id) {

        BigDecimal receitas = repository.obterSaldoPorTipoLancamentoEUsuarioEStatus(id, ReleaseType.RECEITA, LaunchStatus.EFETIVADO);
        BigDecimal despesas = repository.obterSaldoPorTipoLancamentoEUsuarioEStatus(id, ReleaseType.DESPESA, LaunchStatus.EFETIVADO);

        if(receitas == null) {
            receitas = BigDecimal.ZERO;
        }

        if(despesas == null) {
            despesas = BigDecimal.ZERO;
        }

        return receitas.subtract(despesas);

    }

}
