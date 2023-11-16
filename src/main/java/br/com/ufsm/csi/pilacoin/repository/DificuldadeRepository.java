package br.com.ufsm.csi.pilacoin.repository;

import br.com.ufsm.csi.pilacoin.model.DificuldadeJson;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DificuldadeRepository extends JpaRepository<DificuldadeJson, Long> {
}
