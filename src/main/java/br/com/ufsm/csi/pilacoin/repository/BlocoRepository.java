package br.com.ufsm.csi.pilacoin.repository;

import br.com.ufsm.csi.pilacoin.model.Bloco;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlocoRepository extends JpaRepository<Bloco, Long> {
}
