package br.com.ufsm.csi.pilacoin.service;

import br.com.ufsm.csi.pilacoin.model.PilaCoinJson;
import br.com.ufsm.csi.pilacoin.repository.PilaCoinJsonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PilaCoinJsonService {

    @Autowired
    private PilaCoinJsonRepository pilaCoinJsonRepository;

    public void save(PilaCoinJson pilaCoinJson) {
        pilaCoinJsonRepository.save(pilaCoinJson);
    }

    public List<PilaCoinJson> findAll() {
        return pilaCoinJsonRepository.findAll();
    }
}
