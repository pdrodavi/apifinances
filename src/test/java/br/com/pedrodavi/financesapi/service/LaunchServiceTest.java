package br.com.pedrodavi.financesapi.service;

import br.com.pedrodavi.financesapi.enums.LaunchStatus;
import br.com.pedrodavi.financesapi.enums.ReleaseType;
import br.com.pedrodavi.financesapi.exception.BusinessRuleException;
import br.com.pedrodavi.financesapi.model.Launch;
import br.com.pedrodavi.financesapi.model.User;
import br.com.pedrodavi.financesapi.repository.LaunchRepository;
import br.com.pedrodavi.financesapi.repository.LaunchRepositoryTest;
import br.com.pedrodavi.financesapi.service.impl.LaunchServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Example;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LaunchServiceTest {

    @SpyBean
    LaunchServiceImpl service;
    @MockBean
    LaunchRepository repository;

    @Test
    public void deveSalvarUmLancamento() {
        //cenário
        Launch lancamentoASalvar = LaunchRepositoryTest.criarLancamento();
        doNothing().when(service).validar(lancamentoASalvar);

        Launch lancamentoSalvo = LaunchRepositoryTest.criarLancamento();
        lancamentoSalvo.setId(1l);
        lancamentoSalvo.setStatus(LaunchStatus.PENDENTE);
        when(repository.save(lancamentoASalvar)).thenReturn(lancamentoSalvo);

        //execucao
        Launch lancamento = service.salvar(lancamentoASalvar);

        //verificação
        assertThat( lancamento.getId() ).isEqualTo(lancamentoSalvo.getId());
        assertThat(lancamento.getStatus()).isEqualTo(LaunchStatus.PENDENTE);
    }

    @Test
    public void naoDeveSalvarUmLancamentoQuandoHouverErroDeValidacao() {
        //cenário
        Launch lancamentoASalvar = LaunchRepositoryTest.criarLancamento();
        doThrow( BusinessRuleException.class ).when(service).validar(lancamentoASalvar);

        //execucao e verificacao
        catchThrowableOfType( () -> service.salvar(lancamentoASalvar), BusinessRuleException.class );
        verify(repository, never()).save(lancamentoASalvar);
    }

    @Test
    public void deveAtualizarUmLancamento() {
        //cenário
        Launch lancamentoSalvo = LaunchRepositoryTest.criarLancamento();
        lancamentoSalvo.setId(1l);
        lancamentoSalvo.setStatus(LaunchStatus.PENDENTE);

        doNothing().when(service).validar(lancamentoSalvo);

        when(repository.save(lancamentoSalvo)).thenReturn(lancamentoSalvo);

        //execucao
        service.atualizar(lancamentoSalvo);

        //verificação
        verify(repository, times(1)).save(lancamentoSalvo);

    }

    @Test
    public void deveLancarErroAoTentarAtualizarUmLancamentoQueAindaNaoFoiSalvo() {
        //cenário
        Launch lancamento = LaunchRepositoryTest.criarLancamento();

        //execucao e verificacao
        catchThrowableOfType( () -> service.atualizar(lancamento), NullPointerException.class );
        verify(repository, never()).save(lancamento);
    }

    @Test
    public void deveDeletarUmLancamento() {
        //cenário
        Launch lancamento = LaunchRepositoryTest.criarLancamento();
        lancamento.setId(1l);

        //execucao
        service.deletar(lancamento);

        //verificacao
        verify( repository ).delete(lancamento);
    }

    @Test
    public void deveLancarErroAoTentarDeletarUmLancamentoQueAindaNaoFoiSalvo() {

        //cenário
        Launch lancamento = LaunchRepositoryTest.criarLancamento();

        //execucao
        catchThrowableOfType( () -> service.deletar(lancamento), NullPointerException.class );

        //verificacao
        verify( repository, never() ).delete(lancamento);
    }


    @Test
    public void deveFiltrarLancamentos() {
        //cenário
        Launch lancamento = LaunchRepositoryTest.criarLancamento();
        lancamento.setId(1l);

        List<Launch> lista = Arrays.asList(lancamento);
        when( repository.findAll(any(Example.class)) ).thenReturn(lista);

        //execucao
        List<Launch> resultado = service.buscar(lancamento);

        //verificacoes
        assertThat(resultado)
                .isNotEmpty()
                .hasSize(1)
                .contains(lancamento);

    }

    @Test
    public void deveAtualizarOStatusDeUmLancamento() {
        //cenário
        Launch lancamento = LaunchRepositoryTest.criarLancamento();
        lancamento.setId(1l);
        lancamento.setStatus(LaunchStatus.PENDENTE);

        LaunchStatus novoStatus = LaunchStatus.EFETIVADO;
        doReturn(lancamento).when(service).atualizar(lancamento);

        //execucao
        service.atualizarStatus(lancamento, novoStatus);

        //verificacoes
        assertThat(lancamento.getStatus()).isEqualTo(novoStatus);
        verify(service).atualizar(lancamento);

    }

    @Test
    public void deveObterUmLancamentoPorID() {
        //cenário
        Long id = 1l;

        Launch lancamento = LaunchRepositoryTest.criarLancamento();
        lancamento.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(lancamento));

        //execucao
        Optional<Launch> resultado =  service.obterPorId(id);

        //verificacao
        assertThat(resultado.isPresent()).isTrue();
    }

    @Test
    public void deveREtornarVazioQuandoOLancamentoNaoExiste() {
        //cenário
        Long id = 1l;

        Launch lancamento = LaunchRepositoryTest.criarLancamento();
        lancamento.setId(id);

        when( repository.findById(id) ).thenReturn( Optional.empty() );

        //execucao
        Optional<Launch> resultado =  service.obterPorId(id);

        //verificacao
        assertThat(resultado.isPresent()).isFalse();
    }

    @Test
    public void deveLancarErrosAoValidarUmLancamento() {
        Launch lancamento = new Launch();

        Throwable erro = catchThrowable( () -> service.validar(lancamento) );
        assertThat(erro).isInstanceOf(BusinessRuleException.class).hasMessage("Informe uma Descrição válida.");

        lancamento.setDescricao("");

        erro = catchThrowable( () -> service.validar(lancamento) );
        assertThat(erro).isInstanceOf(BusinessRuleException.class).hasMessage("Informe uma Descrição válida.");

        lancamento.setDescricao("Salario");

        erro = catchThrowable( () -> service.validar(lancamento) );
        assertThat(erro).isInstanceOf(BusinessRuleException.class).hasMessage("Informe um Mês válido.");

        lancamento.setAno(0);

        erro = catchThrowable( () -> service.validar(lancamento) );
        assertThat(erro).isInstanceOf(BusinessRuleException.class).hasMessage("Informe um Mês válido.");

        lancamento.setAno(13);

        erro = catchThrowable( () -> service.validar(lancamento) );
        assertThat(erro).isInstanceOf(BusinessRuleException.class).hasMessage("Informe um Mês válido.");

        lancamento.setMes(1);

        erro = catchThrowable( () -> service.validar(lancamento) );
        assertThat(erro).isInstanceOf(BusinessRuleException.class).hasMessage("Informe um Ano válido.");

        lancamento.setAno(202);

        erro = catchThrowable( () -> service.validar(lancamento) );
        assertThat(erro).isInstanceOf(BusinessRuleException.class).hasMessage("Informe um Ano válido.");

        lancamento.setAno(2020);

        erro = catchThrowable( () -> service.validar(lancamento) );
        assertThat(erro).isInstanceOf(BusinessRuleException.class).hasMessage("Informe um Usuário.");

        lancamento.setUsuario(new User());

        erro = catchThrowable( () -> service.validar(lancamento) );
        assertThat(erro).isInstanceOf(BusinessRuleException.class).hasMessage("Informe um Usuário.");

        lancamento.getUsuario().setId(1l);

        erro = catchThrowable( () -> service.validar(lancamento) );
        assertThat(erro).isInstanceOf(BusinessRuleException.class).hasMessage("Informe um Valor válido.");

        lancamento.setValor(BigDecimal.ZERO);

        erro = catchThrowable( () -> service.validar(lancamento) );
        assertThat(erro).isInstanceOf(BusinessRuleException.class).hasMessage("Informe um Valor válido.");

        lancamento.setValor(BigDecimal.valueOf(1));

        erro = catchThrowable( () -> service.validar(lancamento) );
        assertThat(erro).isInstanceOf(BusinessRuleException.class).hasMessage("Informe um tipo de Lançamento.");

    }

    @Test
    public void deveObterSaldoPorUsuario() {
        //cenario
        Long idUsuario = 1l;

        when( repository
                .obterSaldoPorTipoLancamentoEUsuarioEStatus(idUsuario, ReleaseType.RECEITA, LaunchStatus.EFETIVADO))
                .thenReturn(BigDecimal.valueOf(100));

        when( repository
                .obterSaldoPorTipoLancamentoEUsuarioEStatus(idUsuario, ReleaseType.DESPESA, LaunchStatus.EFETIVADO))
                .thenReturn(BigDecimal.valueOf(50));

        //execucao
        BigDecimal saldo = service.obterSaldoPorUsuario(idUsuario);

        //verificacao
        assertThat(saldo).isEqualTo(BigDecimal.valueOf(50));

    }

}
