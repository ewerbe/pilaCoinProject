package br.com.ufsm.csi.pilacoin.service;

import br.com.ufsm.csi.pilacoin.model.Transacao;
import br.com.ufsm.csi.pilacoin.repository.TransacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransacaoService {

    @Autowired
    private TransacaoRepository transacaoRepository;

    public void save(Transacao transacaoPilaCoin) {
        transacaoRepository.save(transacaoPilaCoin);
    }
}
