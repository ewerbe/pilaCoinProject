package br.com.ufsm.csi.pilacoin.controller;


import br.com.ufsm.csi.pilacoin.dto.PilaValidadoDto;
import br.com.ufsm.csi.pilacoin.model.PilaCoinJson;
import br.com.ufsm.csi.pilacoin.model.Usuario;
import br.com.ufsm.csi.pilacoin.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private TransferenciaValidationService transferenciaValidationService;

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

    @GetMapping("/pilas/usuarios")
    private List<Usuario> getUsuarios() {
        try{
            //método para trazer os usuários;
            return usuarioService.findAll();
        }catch (Exception e ){
            e.printStackTrace();
        }
        return null;
    }
    
    @PostMapping("/pilas/transferir")
    private String transferirPila(@RequestBody Long idUsuario) {
        try{
            //método para transferir pila para um usuário escolhido.
            return transferenciaValidationService.transferirPilaCoin(idUsuario);
        }catch (Exception e ){
            e.printStackTrace();
        }
        return null;
    }

}
