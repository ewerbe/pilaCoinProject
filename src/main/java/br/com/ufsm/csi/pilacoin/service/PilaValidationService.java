package br.com.ufsm.csi.pilacoin.service;

import br.com.ufsm.csi.pilacoin.model.PilaCoin;
import br.com.ufsm.csi.pilacoin.model.PilaCoinJson;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Map;

@Service
public class PilaValidationService {

    @Value("clients-msgs")
    private String queue_msgs;

    @Value("clientes-errors")
    private String queue_errors; //tratar se vierem erros

    @Autowired
    private RabbitTemplate rabbitTemplate;
//    @Autowired
//    private MineradoraService mineradoraService;

    public static KeyPair parChaves;
    private static BigInteger dificuldade = null;

    @RabbitListener(queues = {"${queue.dificuldade}"})
    public void receivePilaCoin(@Payload String strPilaCoinJson) {
        //System.out.println("Dificuldade: " + strPilaCoinJson);
        //pegar a dificuldade e passar para o PilaCoin usando objectmapper do jackson;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> retornoDificuldade = objectMapper.readValue(strPilaCoinJson,
                    new TypeReference<Map<String, Object>>() {});
            String dificuldadeStr = retornoDificuldade.get("dificuldade").toString();
            dificuldade = new BigInteger(dificuldadeStr, 16);

            System.out.println("DificuldadeRetorno = " + retornoDificuldade);
            System.out.println("dificuldadeStr = " + dificuldadeStr);
            System.out.println("dificuldade BigInteger = " + dificuldade);
            //chamar o método para minerar;
            minerarPilaCoin(dificuldade, Boolean.TRUE);
        } catch (JsonProcessingException e){
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    private void minerarPilaCoin(BigInteger dificuldade, Boolean mineracaoAtiva) {

        ChaveService chaveService = new ChaveService();
        PilaValidationService pilaValidationService = new PilaValidationService();

        //antes de minerar, vai pegar o par de chaves.
        KeyPair parChaves = chaveService.leParChaves();
        if(mineracaoAtiva) {
            System.out.println("********** INICIANDO MINERAÇÃO DE PILACOINS");
        }
        while(mineracaoAtiva) {
            //pega a dificuldade;
            //cria a hash dos dados e compara com a dificuldade:
            //se for menor: registra o pilaCoin minerado.
            SecureRandom sr = new SecureRandom();
            BigInteger magicNumber = new BigInteger(128, sr);
            //monta o objeto pilaCoin
            PilaCoin pilaCoin = PilaCoin.builder()
                    .dataCriacao(new Date())
                    .chaveCriador(parChaves.getPublic().getEncoded())
                    .nomeCriador("Ewerton")
                    .nonce(magicNumber.toString().getBytes()).build();
            //passa para json e depois cria a hash.
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            String pilaJson = objectMapper.writeValueAsString(pilaCoin);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(pilaJson.getBytes("UTF-8"));
            //apenas valores positivos.
            BigInteger numHashPila = new BigInteger(hash).abs();

            if (numHashPila.compareTo(dificuldade) < 0) {
                System.out.println("MINEROU 1 PILA!!");
                registraPila(pilaCoin , pilaCoin.getNonce());
            }
        }
    }

    private void registraPila(PilaCoin pilaCoin, byte[] nonce) throws JsonProcessingException {
        System.out.println("******** MONTANDO O PILA MINERADO...");
        BigInteger bigIntegerNonce = new BigInteger(nonce);
        PilaCoinJson pilaJson = PilaCoinJson.builder()
                .chaveCriador(pilaCoin.getChaveCriador())
                .dataCriacao(pilaCoin.getDataCriacao())
                .nomeCriador(pilaCoin.getNomeCriador())
                .nonce(bigIntegerNonce.toString())
                .build();
        System.out.println("********************** pilaJson = " + pilaJson);
        ObjectMapper objectMapper = new ObjectMapper();
        String pilaJsonString = objectMapper.writeValueAsString(pilaJson);
        System.out.println("************** PILACOIN MONTADO COM SUCESSO!");
        System.out.println("************** ENVIANDO O PILACOIN...");
        rabbitTemplate.convertAndSend("pila-minerado", pilaJsonString);
        System.out.println("************** PILACOIN ENVIADO COM SUCESSO!");
    }

//    @RabbitListener(queues = {"{queue.valida_pila}"})
//    public String validaPilaMinerado() {
//        //o que implemenar neste método?
//        return null;
//    }

    @RabbitListener(queues = "pila-minerado")
    public void receivePilaCoinMinerado(@Payload String pilaJsonString) {
        try {
            //ver o pila que chegou.
            ObjectMapper objectMapper = new ObjectMapper();
            PilaCoinJson pilaCoinJson = objectMapper.readValue(pilaJsonString, PilaCoinJson.class);
            //ver se o pila é próprio ou de outro minerador:
            if (pilaCoinJson.getNomeCriador().equals("Ewerton")) {
                //se é meu: reenviar pra fila "pila-minerado".
                rabbitTemplate.convertAndSend("pila-minerado", pilaJsonString);
            } else {
                //se é de outro: enviar para método de validação de pilas.
                validaPilas(pilaCoinJson, pilaJsonString);
            }
            System.out.println("************* PilaCoin minerado recebido: " + pilaCoinJson);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao processar a PilaCoin minerada", e);
        }
    }

    @RabbitListener(queues = "ewerton")
    public void receiveMsgFeedbackUser(@Payload String feedback) {
        try {
            System.out.println("*************** Mensagem de feedback recebida: "+ feedback);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao receber mensagem de feedback! ", e);
        }
    }

    private void validaPilas(PilaCoinJson pilaCoinJson, String pilaMineradoRebecido) {
        //fazer try catch: se qualquer erro ocorrer: reenviar o pilaMineradoRebecido para a fila "pila-minerado";
        try{
            //transformar o pila em hash e ver se é menor que a dificuldade;
            PilaCoin pilaCoin = PilaCoin.builder()
                    .dataCriacao(pilaCoinJson.getDataCriacao())
                    .chaveCriador(pilaCoinJson.getChaveCriador())
                    .nomeCriador(pilaCoinJson.getNomeCriador())
                    .nonce(pilaCoinJson.getNonce().getBytes()).build();
            //passa para json e depois cria a hash.
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            String pilaJson = objectMapper.writeValueAsString(pilaCoin);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(pilaJson.getBytes("UTF-8"));
            //apenas valores positivos.
            BigInteger numHashPila = new BigInteger(hash).abs();
            System.out.println("*************** VALIDANDO PILA...");
            if (numHashPila.compareTo(dificuldade) < 0) {
                System.out.println("******************* PILA VALIDADO COM SUCESSO!");
                //TODO: assinaPilaValidado(ENVIAR O QUE DE PARÂMETRO?);
            } else {
                System.out.println("******************* PILA NÃO VÁLIDO!");
                System.out.println("******************* REENVIANDO PARA FILA DE PILAS MINERADOS...");
                rabbitTemplate.convertAndSend("pila-minerado", pilaMineradoRebecido);
                System.out.println("******************* PILA REENVIADO COM SUCESSO!");
            }
        } catch (RuntimeException e) {
            System.out.println("****************** Erro ao validar o pila!");
            System.out.println("****************** Reenviando o pila para a fila 'pila-minerado'");
            rabbitTemplate.convertAndSend("pila-minerado", pilaMineradoRebecido);
            System.out.println("****************** pila enviado com sucesso!");
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }


    }

    private void assinaPilaValidado() {
        //se for menor: instancia o objeto validacaoPilaJson e popula com os atributos do pila;
        //para assinar o pila validado: pego a hash do pila e criptografo com a minha chave privada;
        //populo o atributo assinaturaPilaCoin com a hash criptografada;
        //tranformar o validaCaoPilaJson em json e enviar para fila "pila-validado".
        //se não for: reenvia para fila "pila-minerado"
    }

}
