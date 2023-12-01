package br.com.ufsm.csi.pilacoin.service;

import br.com.ufsm.csi.pilacoin.dto.BlocoValidadoDto;
import br.com.ufsm.csi.pilacoin.repository.BlocoValidadoDtoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BlocoValidadoDtoService {

    @Autowired
    private BlocoValidadoDtoRepository blocoValidadoDtoRepository;
    public void save(BlocoValidadoDto blocoValidadoDto) {
        blocoValidadoDtoRepository.save(blocoValidadoDto);
    }
}
