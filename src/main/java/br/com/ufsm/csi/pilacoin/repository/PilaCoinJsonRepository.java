package br.com.ufsm.csi.pilacoin.repository;

import br.com.ufsm.csi.pilacoin.model.PilaCoinJson;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PilaCoinJsonRepository extends JpaRepository<PilaCoinJson, Long> {
}
