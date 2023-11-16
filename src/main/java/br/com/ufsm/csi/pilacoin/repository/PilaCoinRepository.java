package br.com.ufsm.csi.pilacoin.repository;

import br.com.ufsm.csi.pilacoin.model.PilaCoin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PilaCoinRepository extends JpaRepository<PilaCoin, Long> {
}
