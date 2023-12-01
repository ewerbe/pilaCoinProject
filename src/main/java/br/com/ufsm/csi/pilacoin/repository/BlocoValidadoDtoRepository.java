package br.com.ufsm.csi.pilacoin.repository;


import br.com.ufsm.csi.pilacoin.dto.BlocoValidadoDto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlocoValidadoDtoRepository extends JpaRepository<BlocoValidadoDto, Long> {

}
