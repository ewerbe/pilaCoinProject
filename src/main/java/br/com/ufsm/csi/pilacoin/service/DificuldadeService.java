package br.com.ufsm.csi.pilacoin.service;

import br.com.ufsm.csi.pilacoin.model.DificuldadeJson;
import br.com.ufsm.csi.pilacoin.repository.DificuldadeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DificuldadeService {

    @Autowired
    private DificuldadeRepository dificuldadeRepository;

    public void save(DificuldadeJson dificuldadeJson) {
        dificuldadeRepository.save(dificuldadeJson);
    }

    public DificuldadeJson getLastDificuldade() {
        return dificuldadeRepository.getLastDificuldade();
    }
}

