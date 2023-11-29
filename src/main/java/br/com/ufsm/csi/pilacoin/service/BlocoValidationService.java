package br.com.ufsm.csi.pilacoin.service;

import br.com.ufsm.csi.pilacoin.model.Bloco;
import br.com.ufsm.csi.pilacoin.model.DificuldadeJson;
import br.com.ufsm.csi.pilacoin.model.PilaCoin;
import br.com.ufsm.csi.pilacoin.model.Transacao;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

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
        JsonNode node = mapper.readTree(strBloco);
        mineraBloco();
    }

    private void mineraBloco() throws JsonProcessingException,
                            NoSuchAlgorithmException, UnsupportedEncodingException {
        Bloco bloco = getBloco();
        Boolean mineracaoBloco = Boolean.TRUE;
        // dificuldade = new BigInteger(dificuldadeStr, 16).abs();
        dificuldade = new BigInteger(getDificuldade().getDificuldade(), 16).abs();
        while (mineracaoBloco) {
            bloco.setNonce(getNonceBloco());
            String blocoJson = new ObjectMapper().writeValueAsString(bloco);
            // Gera um hash SHA-256 para o bloco.
            MessageDigest md = MessageDigest.getInstance("sha-256");
            byte[] hash = md.digest(blocoJson.getBytes("UTF-8"));
            BigInteger numHash = new BigInteger(hash).abs();
            // Compara o hash gerado com a dificuldade
            if (numHash.compareTo(dificuldade) < 0) {
                System.out.println("******************* 1 BLOCO MINERADO! *************************");
                ObjectMapper objectMapper = new ObjectMapper();
                String blocoMineradoStr = objectMapper.writeValueAsString(bloco);
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

    private Bloco getBloco() {
        parChaves = chaveService.leParChaves();
        return Bloco.builder()
                .chaveUsuarioMinerador(parChaves.getPublic().getEncoded())
                .nomeUsuarioMinerador("ewerton-joaokunde")
                .build();
    }

    private String getNonceBloco() {
        SecureRandom rnd = new SecureRandom();
        return new BigInteger(128, rnd).toString();
    }

}