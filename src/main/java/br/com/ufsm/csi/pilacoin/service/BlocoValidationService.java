package br.com.ufsm.csi.pilacoin.service;

import br.com.ufsm.csi.pilacoin.model.Bloco;
import br.com.ufsm.csi.pilacoin.model.DificuldadeJson;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Service
public class BlocoValidationService {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private DificuldadeService dificuldadeService;
    @Autowired
    private BlocoService blocoService;
    @Autowired
    private ChaveService chaveService;

    public static KeyPair parChaves = null;
    private static BigInteger dificuldade = BigInteger.ZERO;

    @RabbitListener(queues = {"descobre-bloco"})
    public void descobreBloco(@Payload String strBloco) throws JsonProcessingException,
                                UnsupportedEncodingException, NoSuchAlgorithmException {
        System.out.println("*********************** BLOCO RECEBIDO: " + strBloco);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode nodeBloco = mapper.readTree(strBloco);
        System.out.println("************************* node.get('nomeUsuarioMinerador') do bloco = " + nodeBloco.get("nomeUsuarioMinerador"));
        String nomeUsuarioMineradorBlocoPreenchido = null;
        if(nodeBloco.get("nomeUsuarioMinerador") != null) {
            nomeUsuarioMineradorBlocoPreenchido = nodeBloco.get("nomeUsuarioMinerador").toString();
        }
        if(nomeUsuarioMineradorBlocoPreenchido == null ||
                nomeUsuarioMineradorBlocoPreenchido.isEmpty()) {
            mineraBloco(strBloco);
        } else {
            System.out.println("************************* BLOCO JÁ MINERADO POR OUTRO...");
        }
    }

    private void mineraBloco(String blocoStr) throws JsonProcessingException,
                            NoSuchAlgorithmException, UnsupportedEncodingException {
//        Bloco bloco = getBloco(nodeBloco);
        Boolean mineracaoBloco = Boolean.TRUE;
        ObjectMapper objectMapper = new ObjectMapper();
        //transforma a string do bloco em instância de Bloco.
        Bloco bloco = objectMapper.readValue(blocoStr, Bloco.class);
        parChaves = chaveService.leParChaves();
        bloco.setChaveUsuarioMinerador(parChaves.getPublic().getEncoded());
        bloco.setNomeUsuarioMinerador("ewerton-joaokunde");
        DificuldadeJson dificuldadeJson = getDificuldade();
        dificuldade = new BigInteger(dificuldadeJson.getDificuldade(), 16).abs();
        System.out.println("******************************** INICIANDO MINERAÇÃO DO BLOCO...");
        while (mineracaoBloco) {
            System.out.println("********************************************* MINERANDO...");
            //setando o nonce para o bloco.
            bloco.setNonce(getNonceBloco());
            //transformando o bloco em String.
            String blocoJson = new ObjectMapper().writeValueAsString(bloco);
            MessageDigest md = MessageDigest.getInstance("sha-256");
            //transformando a string do bloco em hash.
            byte[] hash = md.digest(blocoJson.getBytes("UTF-8"));
            BigInteger numHash = new BigInteger(hash).abs();
            // Compara o hash gerado com a dificuldade
            if (numHash.compareTo(dificuldade) < 0) {
                System.out.println("******************* 1 BLOCO MINERADO! *************************");
                ObjectMapper om = new ObjectMapper();
                String blocoMineradoStr = om.writeValueAsString(bloco);
                blocoService.save(bloco);
                System.out.println("******************************* BLOCO SALVO COM SUCESSO! ENVIANDO PARA FILA...");
                rabbitTemplate.convertAndSend("bloco-minerado", blocoMineradoStr);
                System.out.println("******************************** BLOCO ENVIADO COM SUCESSO!");
                mineracaoBloco = Boolean.FALSE;
            }
        }
    }

    private DificuldadeJson getDificuldade() {
        return dificuldadeService.getLastDificuldade();
    }

    private String getNonceBloco() {
        SecureRandom rnd = new SecureRandom();
        return new BigInteger(128, rnd).toString();
    }

}