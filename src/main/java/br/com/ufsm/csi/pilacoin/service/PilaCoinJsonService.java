package br.com.ufsm.csi.pilacoin.service;

import br.com.ufsm.csi.pilacoin.model.PilaCoinJson;
import br.com.ufsm.csi.pilacoin.repository.PilaCoinJsonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PilaCoinJsonService {

    @Autowired
    private PilaCoinJsonRepository pilaCoinJsonRepository;

    public void save(PilaCoinJson pilaCoinJson) {
        pilaCoinJsonRepository.save(pilaCoinJson);
    }
}
