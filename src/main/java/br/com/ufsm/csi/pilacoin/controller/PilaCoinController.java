package br.com.ufsm.csi.pilacoin.controller;


import br.com.ufsm.csi.pilacoin.dto.PilaValidadoDto;
import br.com.ufsm.csi.pilacoin.model.PilaCoinJson;
import br.com.ufsm.csi.pilacoin.service.PilaCoinJsonService;
import br.com.ufsm.csi.pilacoin.service.PilaValidadoDtoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;

@RestController
public class PilaCoinController {

    @Autowired
    private PilaCoinJsonService pilaCoinJsonService;
    @Autowired
    private PilaValidadoDtoService pilaValidadoDtoService;

    //Endpoint pra lista os pilas minerados. Estes pilas ainda não são válidos para transação,
    // então: não contar como Saldo.
    @GetMapping("/pilas/minerados")
        private List<PilaCoinJson> getMinerados() {
        List<PilaCoinJson> minerados = new LinkedList<>();
        try{
            minerados = pilaCoinJsonService.findAll();
        }catch (Exception e ){
            e.printStackTrace();
        }
        return minerados;
        }

    @GetMapping("/pilas/validados/outros")
    private List<PilaValidadoDto> getValidadosOutros() {
        List<PilaValidadoDto> validadosOutros = new LinkedList<>();
        try{
            validadosOutros = pilaValidadoDtoService.findAll();
        }catch (Exception e ){
            e.printStackTrace();
        }
        return validadosOutros;
    }
}
