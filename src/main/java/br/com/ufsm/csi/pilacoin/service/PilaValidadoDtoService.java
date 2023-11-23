package br.com.ufsm.csi.pilacoin.service;

import br.com.ufsm.csi.pilacoin.dto.PilaValidadoDto;
import br.com.ufsm.csi.pilacoin.repository.PilaValidadoDtoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PilaValidadoDtoService {

    @Autowired
    private PilaValidadoDtoRepository pilaValidadoDtoRepository;

    public void save(PilaValidadoDto pilaValidadoDto) {
        pilaValidadoDtoRepository.save(pilaValidadoDto);
    }
}
