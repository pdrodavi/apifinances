package br.com.pedrodavi.financesapi.repository;

import br.com.pedrodavi.financesapi.enums.LaunchStatus;
import br.com.pedrodavi.financesapi.enums.ReleaseType;
import br.com.pedrodavi.financesapi.model.Launch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class LaunchRepositoryTest {

    @Autowired
    LaunchRepository repository;

    @Autowired
    TestEntityManager entityManager;

    @Test
    public void deveSalvarUmLancamento() {
        Launch lancamento = criarLancamento();

        lancamento = repository.save(lancamento);

        assertThat(lancamento.getId()).isNotNull();
    }

    @Test
    public void deveDeletarUmLancamento() {
        Launch lancamento = criarEPersistirUmLancamento();

        lancamento = entityManager.find(Launch.class, lancamento.getId());

        repository.delete(lancamento);

        Launch lancamentoInexistente = entityManager.find(Launch.class, lancamento.getId());
        assertThat(lancamentoInexistente).isNull();
    }


    @Test
    public void deveAtualizarUmLancamento() {
        Launch lancamento = criarEPersistirUmLancamento();

        lancamento.setAno(2018);
        lancamento.setDescricao("Teste Atualizar");
        lancamento.setStatus(LaunchStatus.CANCELADO);

        repository.save(lancamento);

        Launch lancamentoAtualizado = entityManager.find(Launch.class, lancamento.getId());

        assertThat(lancamentoAtualizado.getAno()).isEqualTo(2018);
        assertThat(lancamentoAtualizado.getDescricao()).isEqualTo("Teste Atualizar");
        assertThat(lancamentoAtualizado.getStatus()).isEqualTo(LaunchStatus.CANCELADO);
    }

    @Test
    public void deveBuscarUmLancamentoPorId() {
        Launch lancamento = criarEPersistirUmLancamento();

        Optional<Launch> lancamentoEncontrado = repository.findById(lancamento.getId());

        assertThat(lancamentoEncontrado.isPresent()).isTrue();
    }

    private Launch criarEPersistirUmLancamento() {
        Launch lancamento = criarLancamento();
        entityManager.persist(lancamento);
        return lancamento;
    }

    public static Launch criarLancamento() {
        return Launch.builder()
                .ano(2019)
                .mes(1)
                .descricao("lancamento qualquer")
                .valor(BigDecimal.valueOf(10))
                .tipo(ReleaseType.RECEITA)
                .status(LaunchStatus.PENDENTE)
                .dataCadastro(LocalDate.now())
                .build();
    }

}
