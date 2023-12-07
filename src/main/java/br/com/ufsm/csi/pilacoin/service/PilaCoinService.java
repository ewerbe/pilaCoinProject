package br.com.ufsm.csi.pilacoin.service;

import br.com.ufsm.csi.pilacoin.model.PilaCoin;
import br.com.ufsm.csi.pilacoin.repository.PilaCoinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
