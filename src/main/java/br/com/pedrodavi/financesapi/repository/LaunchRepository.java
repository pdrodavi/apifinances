package br.com.pedrodavi.financesapi.repository;

import br.com.pedrodavi.financesapi.enums.LaunchStatus;
import br.com.pedrodavi.financesapi.enums.ReleaseType;
import br.com.pedrodavi.financesapi.model.Launch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface LaunchRepository extends JpaRepository<Launch, Long> {

    @Query( value =
            " select sum(l.valor) from Launch l join l.usuario u "
                    + " where u.id = :idUsuario and l.tipo =:tipo and l.status = :status group by u " )
    BigDecimal obterSaldoPorTipoLancamentoEUsuarioEStatus(
            @Param("idUsuario") Long idUsuario,
            @Param("tipo") ReleaseType tipo,
            @Param("status") LaunchStatus status);

}
