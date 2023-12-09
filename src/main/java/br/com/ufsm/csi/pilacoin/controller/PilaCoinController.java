package br.com.ufsm.csi.pilacoin.controller;


import br.com.ufsm.csi.pilacoin.dto.PilaValidadoDto;
import br.com.ufsm.csi.pilacoin.model.PilaCoinJson;
import br.com.ufsm.csi.pilacoin.service.PilaCoinJsonService;
import br.com.ufsm.csi.pilacoin.service.PilaCoinService;
import br.com.ufsm.csi.pilacoin.service.PilaValidadoDtoService;
import br.com.ufsm.csi.pilacoin.service.PilaValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class PilaCoinController {

    @Autowired
    private PilaCoinJsonService pilaCoinJsonService;
    @Autowired
    private PilaValidadoDtoService pilaValidadoDtoService;
    @Autowired
    private PilaCoinService pilaCoinService;
    @Autowired
    private PilaValidationService pilaValidationService;

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

    @GetMapping("/pilas/saldo")
    private Integer getSaldo() {
        try{
            return pilaCoinService.getSaldo();
        }catch (Exception e ){
            e.printStackTrace();
        }
        return null;
    }

    @GetMapping("/pilas/minerar")
    private String minerarPilaCoins() {
        try{
            //método para ligar as filas de mineração de pilas;
            return pilaValidationService.iniciaMineracao();
        }catch (Exception e ){
            e.printStackTrace();
        }
        return null;
    }

    @GetMapping("/pilas/parar-mineracao")
    private String pararMineracaoPilaCoins() {
        try{
            //método para inativar as filas de mineração de pilas;
            return pilaValidationService.paraMineracao();
        }catch (Exception e ){
            e.printStackTrace();
        }
        return null;
    }

}
