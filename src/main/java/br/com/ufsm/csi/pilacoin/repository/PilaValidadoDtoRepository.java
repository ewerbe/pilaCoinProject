package br.com.ufsm.csi.pilacoin.repository;

import br.com.ufsm.csi.pilacoin.dto.PilaValidadoDto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PilaValidadoDtoRepository extends JpaRepository<PilaValidadoDto, Long> {
}
