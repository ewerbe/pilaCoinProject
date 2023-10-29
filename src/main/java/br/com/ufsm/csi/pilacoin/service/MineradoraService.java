//package br.com.ufsm.csi.pilacoin.service;
//
//import br.com.ufsm.csi.pilacoin.model.PilaCoin;
//import com.fasterxml.jackson.annotation.JsonInclude;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.SneakyThrows;
//import org.apache.tomcat.util.codec.binary.Base64;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.math.BigInteger;
//import java.security.KeyPair;
//import java.security.MessageDigest;
//import java.security.SecureRandom;
//import java.util.Date;
//
//@Service
//public class MineradoraService {
//
////    @Autowired
////    private PilaCoinRepository pilaCoinRepository;
//    @Autowired
//    private RabbitTemplate rabbitTemplate;
//
//    //public static BigInteger dificuldade = BigInteger.ZERO;
//    //public static Boolean mineracaoAtiva = Boolean.FALSE;
//    public static KeyPair parChaves;
//
//    @SneakyThrows
//    public void minerarPilaCoin(BigInteger dificuldade, Boolean mineracaoAtiva) {
//
//        ChaveService chaveService = new ChaveService();
//        PilaValidationService pilaValidationService = new PilaValidationService();
//
//        //antes de minerar, vai pegar o par de chaves.
//        KeyPair parChaves = chaveService.leParChaves();
//        if(mineracaoAtiva) {
//            System.out.println("********** INICIANDO MINERAÇÃO DE PILACOINS");
//        }
//        while(mineracaoAtiva) {
//            //pega a dificuldade;
//            //cria a hash dos dados e compara com a dificuldade:
//            //se for menor: registra o pilaCoin minerado.
//            SecureRandom sr = new SecureRandom();
//            BigInteger magicNumber = new BigInteger(128, sr);
//            PilaCoin pilaCoin = PilaCoin.builder()
//                    .dataCriacao(new Date())
//                    .chaveCriador(parChaves.getPublic().getEncoded())
//                    .nonce(magicNumber.toString()).build();
//            ObjectMapper objectMapper = new ObjectMapper();
//            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//            String pilaJson = objectMapper.writeValueAsString(pilaCoin);
//            MessageDigest md = MessageDigest.getInstance("SHA-256");
//            byte[] hash = md.digest(pilaJson.getBytes("UTF-8"));
//            BigInteger numHashPila = new BigInteger(hash).abs();
//
//            if (numHashPila.compareTo(dificuldade) < 0) {
//                System.out.println(Base64.encodeBase64String(keyPair.getPublic().getEncoded()));
//                System.out.println("MINEROU 1 PILA!!");
//                //registraPila(pilaJson , pilaCoin.getNonce());
//
//            }
//        }
//    }
//}
