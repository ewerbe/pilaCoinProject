package br.com.ufsm.csi.pilacoin.service;

import br.com.ufsm.csi.pilacoin.model.ValidacaoPilaJson;
import br.com.ufsm.csi.pilacoin.repository.ValidacaoPilaJsonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ValidacaoPilaJsonService {

    @Autowired
    private ValidacaoPilaJsonRepository validacaoPilaJsonRepository;

    public void save (ValidacaoPilaJson validacaoPilaJson){
        validacaoPilaJsonRepository.save(validacaoPilaJson);
    }

    public void saveAll(List<ValidacaoPilaJson> pilaCoinList) {
        validacaoPilaJsonRepository.saveAll(pilaCoinList);
    }
}
