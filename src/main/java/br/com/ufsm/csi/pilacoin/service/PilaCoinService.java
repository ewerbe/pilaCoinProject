package br.com.ufsm.csi.pilacoin.service;

import br.com.ufsm.csi.pilacoin.model.PilaCoin;
import br.com.ufsm.csi.pilacoin.repository.PilaCoinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
public class PilaCoinService {

    @Autowired
    private PilaCoinRepository pilaCoinRepository;

    public void save(PilaCoin pilaCoin) {
        pilaCoinRepository.save(pilaCoin);
    }

    public void saveAll(List<PilaCoin> pilaCoinList) {
        pilaCoinRepository.saveAll(pilaCoinList);
    }

    public Optional<PilaCoin> findById(Long idPila) {
        return pilaCoinRepository.findById(idPila);
    }

    public Integer getSaldo() {
        List<PilaCoin> listaTodosPilas = this.findAll();
        Integer saldo = 0;
        for(PilaCoin pila : listaTodosPilas) {
            if(pila.getStatus() != null && pila.getStatus().equals("VALIDO")) {
                saldo = saldo + 1;
            }
        }
        return saldo;
    }

    public List<PilaCoin> findAll() {
        return pilaCoinRepository.findAll();
    }

    public PilaCoin getPilaCoinValidoParaTransferir() {
        List<PilaCoin> listaPilas = this.findAll();
        PilaCoin pilaRetorno = null;
        for(PilaCoin pila : listaPilas) {
            if(pila.getStatus().equals("VALIDO") && pila.getTransacoes().isEmpty()) {
                pilaRetorno = pila;
                return pilaRetorno;
            }
        }
        return pilaRetorno;
    }
}
