package br.com.ufsm.csi.pilacoin.repository;

import br.com.ufsm.csi.pilacoin.model.DificuldadeJson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DificuldadeRepository extends JpaRepository<DificuldadeJson, Long> {

    @Query("SELECT d FROM DificuldadeJson d ORDER BY d.dificuldade_json_seq DESC")
    DificuldadeJson getLastDificuldade();
}
