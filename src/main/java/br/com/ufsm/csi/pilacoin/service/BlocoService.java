package br.com.ufsm.csi.pilacoin.service;

import br.com.ufsm.csi.pilacoin.model.Bloco;
import br.com.ufsm.csi.pilacoin.repository.BlocoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BlocoService {

    @Autowired
    private BlocoRepository blocoRepository;

    public void save(Bloco bloco) {
        blocoRepository.save(bloco);
    }
}
