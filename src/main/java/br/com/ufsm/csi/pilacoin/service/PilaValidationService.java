package br.com.ufsm.csi.pilacoin.service;

import br.com.ufsm.csi.pilacoin.dto.PilaValidadoDto;
import br.com.ufsm.csi.pilacoin.model.DificuldadeJson;
import br.com.ufsm.csi.pilacoin.model.PilaCoin;
//import br.com.ufsm.csi.pilacoin.model.PilaCoinJson;
import br.com.ufsm.csi.pilacoin.model.PilaCoinJson;
import br.com.ufsm.csi.pilacoin.model.ValidacaoPilaJson;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.SneakyThrows;
import org.hibernate.exception.DataException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Date;
import java.util.Map;
import java.util.Random;

@Service
public class PilaValidationService {

    @Value("clients-msgs")
    private String queue_msgs;

    @Value("clientes-errors")
    private String queue_errors;

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private DificuldadeService dificuldadeService;
    @Autowired
    private PilaCoinService pilaCoinService;
    @Autowired
    private ValidacaoPilaJsonService validacaoPilaJsonService;
    @Autowired
    private PilaValidadoDtoService pilaValidadoDtoService;
    @Autowired
    private PilaCoinJsonService pilaCoinJsonService;

    public static KeyPair parChaves = null;
    private static BigInteger dificuldade = BigInteger.ZERO;

    @RabbitListener(queues = {"${queue.dificuldade}"})
    public void receivePilaCoin(@Payload String strPilaCoinJson) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> retornoDificuldade = objectMapper.readValue(strPilaCoinJson,
                    new TypeReference<Map<String, Object>>() {});
            String dificuldadeStr = retornoDificuldade.get("dificuldade").toString();
            dificuldade = new BigInteger(dificuldadeStr, 16).abs();
            DificuldadeJson dificuldadePersistencia = new DificuldadeJson();
            dificuldadePersistencia.setDificuldade(dificuldadeStr);
            dificuldadePersistencia.setInicio(retornoDificuldade.get("inicio").toString());
            dificuldadePersistencia.setValidadeFinal(retornoDificuldade.get("validadeFinal").toString());
            dificuldadeService.save(dificuldadePersistencia);

            System.out.println("DificuldadeRetorno = " + retornoDificuldade);
            System.out.println("dificuldadeStr = " + dificuldadeStr);
            System.out.println("dificuldade BigInteger = " + dificuldade);
            //chamar o método para minerar;
            minerarPilaCoin(dificuldade, Boolean.TRUE);
        } catch (JsonProcessingException e){
            throw new RuntimeException(e);
        }
    }
    //TODO: retirar a criação do objeto Pilacoin do laço while ***************************
    @SneakyThrows
    private void minerarPilaCoin(BigInteger dificuldade, Boolean mineracaoAtiva) {

        ChaveService chaveService = new ChaveService();
        //antes de minerar, vai pegar o par de chaves.
        parChaves = chaveService.leParChaves();
        if(mineracaoAtiva) {
            System.out.println("********** INICIANDO MINERAÇÃO DE PILACOINS");
        }
        //TODO: chamar método para a criação do ojjeto pilacoin
        PilaCoin pilaCoin = getPilaCoin();
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        byte[] byteArray = new byte[256 / 8];
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        while(mineracaoAtiva) {
            //cria a hash dos dados e compara com a dificuldade.
            SecureRandom random = getSecureRandom();
            random.nextBytes(byteArray);
            pilaCoin.setNonce(new BigInteger(md.digest(byteArray)).abs().toString());
            //passa para json e depois cria a hash.
            String pilaStringJson = ow.writeValueAsString(pilaCoin);
            BigInteger numHashPila = new BigInteger(md.digest(pilaStringJson.getBytes(StandardCharsets.UTF_8))).abs();

            if (numHashPila.compareTo(dificuldade) < 0) {
                System.out.println("*****************************************************************************");
                System.out.println("**************************** MINEROU 1 PILA!!");
                System.out.println("**************************** ENVIANDO PILA MINERADO...");
                System.out.println("**************************** SALVANDO PILA EM BANCO...");
                //salvando o pila no BD.
                pilaCoinService.save(pilaCoin);
                System.out.println("**************************** SALVO COM SUCESSO!");
                rabbitTemplate.convertAndSend("pila-minerado", pilaStringJson);
                System.out.println("**************************** PILA ENVIADO COM SUCESSO!");
                System.out.println("****************************************************************************");
            }
        }
    }

    @RabbitListener(queues = "pila-minerado")
    public void receivePilaCoinMinerado(@Payload String pilaJsonString) {
        try {
            //ver o pila que chegou.
            ObjectMapper objectMapper = new ObjectMapper();
            PilaCoin pilaCoin = objectMapper.readValue(pilaJsonString, PilaCoin.class);
            //ver se o pila é próprio ou de outro minerador:
            System.out.println("*************************************************************************************");
            System.out.println("********************* PILA MINERADO RECEBIDO!");
            if (pilaCoin.getNomeCriador().equals("ewerton-joaokunde")) {
                System.out.println("******************** PILA MINERADO RECEBIDO É PRÓPRIO!");
                //se é meu: reenviar pra fila "pila-minerado".
                System.out.println("******************** REENVIANDO PILA MINERADO RECEBIDO PARA FILA pila-minerado...");
                rabbitTemplate.convertAndSend("pila-minerado", pilaJsonString);
                System.out.println("******************** PILA MINERADO RECEBIDO REENVIADO COM SUCESSO!");
                System.out.println("*************************************************************************************");
            } else {
                //se é de outro: enviar para método de validação de pilas.
                System.out.println("******************** PILA MINERADO RECEBIDO NÃO É PRÓPRIO...");
                System.out.println("******************** ENVIANDO PARA VALIDAÇÃO...");
                System.out.println("*************************************************************************************");
                validaPilas(pilaCoin, pilaJsonString);
            }
            System.out.println("************* PilaCoin minerado recebido: " + pilaCoin);

        } catch (JsonProcessingException e) {
            System.out.println("*************************************************************************************");
            System.out.println("************** ERRO COM PILACOIN RECEBIDO...REENVIANDO...");
            System.out.println("*************************************************************************************");
            rabbitTemplate.convertAndSend("pila-minerado", pilaJsonString);
            //throw new RuntimeException("Erro ao processar a PilaCoin minerada", e);
        }
    }

    //ativar esta audição da fila ewerton-joaokunde apenas depois de minerar algum pila, pois, senão, ela não existirá.
    @RabbitListener(queues = "ewerton-joaokunde")
    public void receiveMsgFeedbackUser(@Payload String feedback) {
        try {
            System.out.println("*************** Mensagem de feedback recebida: "+ feedback);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao receber mensagem de feedback! ", e);
        }
    }

    //fila para relatório de funcionalidades desenvolvidas no projeto.
//    @RabbitListener(queues = "report")
//    public void receiveUsersReport(@Payload String usersReport) {
//        try {
//            System.out.println("*************** Relatório recebido: "+ usersReport);
//        } catch (Exception e) {
//            throw new RuntimeException("*********** Erro ao receber relatório! ", e);
//        }
//    }

    private void validaPilas(PilaCoin pilaCoin, String pilaMineradoRebecido) {
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            BigInteger numHashPila = new BigInteger(md.digest(pilaMineradoRebecido.getBytes(StandardCharsets.UTF_8))).abs();
            byte[] hash = md.digest(pilaMineradoRebecido.getBytes(StandardCharsets.UTF_8));
            if (numHashPila.compareTo(dificuldade) < 0) {
                System.out.println("******************* PILA VALIDADO COM SUCESSO!");
                System.out.println("*************************************************************************************");
                //assinar o pila e enviar para o server.
                assinaPilaValidado(pilaCoin, hash);
            } else {
                System.out.println("******************* PILA NÃO VÁLIDO!");
                System.out.println("******************* REENVIANDO PARA FILA DE PILAS MINERADOS...");
                rabbitTemplate.convertAndSend("pila-minerado", pilaMineradoRebecido);
                System.out.println("******************* PILA REENVIADO COM SUCESSO!");
                System.out.println("*************************************************************************************");
            }
        } catch (RuntimeException e) {
            System.out.println("****************** ERRO AO VALIDAR O PILA!");
            System.out.println("****************** REENVIANDO O PILA PARA A FILA 'pila-minerado'");
            rabbitTemplate.convertAndSend("pila-minerado", pilaMineradoRebecido);
            System.out.println("****************** PILA ENVIADO COM SUCESSO!");
            System.out.println("*************************************************************************************");
        } catch (NoSuchAlgorithmException |
                 InvalidKeyException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void assinaPilaValidado(PilaCoin pilaCoinRecebido,
                                    byte[] hash)
            throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException, JsonProcessingException {
        //populo o atributo assinaturaPilaCoin com a hash criptografada;
        Cipher cipherRSA = Cipher.getInstance("RSA");
        //iniciar o cipherRSA com o modo encriptografador;
        cipherRSA.init(Cipher.ENCRYPT_MODE, parChaves.getPrivate());
        System.out.println("*************************************************************************************");
        System.out.println("******************* ASSINANDO PILACOIN VALIDADO...");
        byte[] assinaturaPilaCoinJson = cipherRSA.doFinal(hash);
        System.out.println("******************* PILACOIN ASSINADO COM SUCESSO!");

        //instanciando o pilacoin validado para envio na fila pila-validado.
        ValidacaoPilaJson validacaoPilaJson = ValidacaoPilaJson.builder()
                .nomeValidador("ewerton-joaokunde")
                .chavePublicaValidador(parChaves.getPublic().getEncoded())
                .assinaturaPilaCoin(assinaturaPilaCoinJson)
                .pilaCoinJson(pilaCoinRecebido).build();
        System.out.println("****************** SALVANDO PILA VALIDADO EM BANCO...");

        PilaValidadoDto pilaValidadoDto = PilaValidadoDto.builder()
                .nomeValidador(validacaoPilaJson.getNomeValidador())
                .chavePublicaValidador(validacaoPilaJson.getChavePublicaValidador().toString())
                .assinaturaPilaCoin(validacaoPilaJson.getAssinaturaPilaCoin().toString())
                .noncePilaCoinValidado(validacaoPilaJson.getPilaCoinJson().getNonce().toString())
                .nomeCriadorPilaCoin(validacaoPilaJson.getPilaCoinJson().getNomeCriador()).build();
        pilaValidadoDtoService.save(pilaValidadoDto);
        //validacaoPilaJsonService.save(validacaoPilaJson);
        System.out.println("****************** SALVO COM SUCESSO!");

        System.out.println("****************** ENVIANDO PILACOIN VALIDADO PARA FILA pila-validado...");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String validacaoPilaJsonString = ow.writeValueAsString(validacaoPilaJson);
        rabbitTemplate.convertAndSend("pila-validado", validacaoPilaJsonString);
        System.out.println("****************** PILACOIN VALIDADO ENVIADO COM SUCESSO!");
        System.out.println("*************************************************************************************");
    }

    private PilaCoin getPilaCoin() {
        return PilaCoin.builder()
                .chaveCriador(parChaves.getPublic().getEncoded())
                .nomeCriador("ewerton-joaokunde")
                .dataCriacao(new Date())
                .build();
    }

    private SecureRandom getSecureRandom() {
        return new SecureRandom();
    }

}
