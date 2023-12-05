//package br.com.ufsm.csi.pilacoin.service;
//
//import br.com.ufsm.csi.pilacoin.dto.BlocoValidadoDto;
//import br.com.ufsm.csi.pilacoin.model.*;
//import com.fasterxml.jackson.annotation.JsonInclude;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.ObjectWriter;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.messaging.handler.annotation.Payload;
//import org.springframework.stereotype.Service;
//
//import javax.crypto.BadPaddingException;
//import javax.crypto.Cipher;
//import javax.crypto.IllegalBlockSizeException;
//import javax.crypto.NoSuchPaddingException;
//import java.io.UnsupportedEncodingException;
//import java.math.BigInteger;
//import java.nio.charset.StandardCharsets;
//import java.security.*;
//
//@Service
//public class BlocoValidationService {
//
//    @Autowired
//    private RabbitTemplate rabbitTemplate;
//    @Autowired
//    private DificuldadeService dificuldadeService;
//    @Autowired
//    private BlocoService blocoService;
//    @Autowired
//    private ChaveService chaveService;
//    @Autowired
//    private BlocoValidadoDtoService blocoValidadoDtoService;
//
//    public static KeyPair parChaves = null;
//    private static BigInteger dificuldade = BigInteger.ZERO;
//
//    @RabbitListener(queues = {"descobre-bloco"})
//    public void descobreBloco(@Payload String strBloco) throws JsonProcessingException,
//                                UnsupportedEncodingException, NoSuchAlgorithmException {
//        System.out.println("*********************** BLOCO RECEBIDO: " + strBloco);
//        ObjectMapper mapper = new ObjectMapper();
//        JsonNode nodeBloco = mapper.readTree(strBloco);
//        System.out.println("************************* node.get('nomeUsuarioMinerador') do bloco = " + nodeBloco.get("nomeUsuarioMinerador"));
//        String nomeUsuarioMineradorBlocoPreenchido = null;
//        if(nodeBloco.get("nomeUsuarioMinerador") != null) {
//            nomeUsuarioMineradorBlocoPreenchido = nodeBloco.get("nomeUsuarioMinerador").toString();
//        }
//        if(nomeUsuarioMineradorBlocoPreenchido == null ||
//                nomeUsuarioMineradorBlocoPreenchido.isEmpty()) {
//            mineraBloco(strBloco);
//        } else {
//            System.out.println("************************* BLOCO JÁ MINERADO POR OUTRO...");
//        }
//    }
//
//    @RabbitListener(queues = "bloco-minerado")
//    public void receiveBlocoMinerado(@Payload String blocoJsonString) {
//        try {
//            //ver o pila que chegou.
//            ObjectMapper objectMapper = new ObjectMapper();
//            Bloco bloco = objectMapper.readValue(blocoJsonString, Bloco.class);
//            //ver se o pila é próprio ou de outro minerador:
//            System.out.println("*************************************************************************************");
//            System.out.println("********************* BLOCO MINERADO RECEBIDO!");
//            if (bloco.getNomeUsuarioMinerador().equals("ewerton-joaokunde")) {
//                System.out.println("******************** BLOCO MINERADO RECEBIDO É PRÓPRIO!");
//                //se é meu: reenviar pra fila "bloco-minerado".
//                System.out.println("******************** REENVIANDO BLOCO MINERADO RECEBIDO PARA FILA bloco-minerado...");
//                rabbitTemplate.convertAndSend("bloco-minerado", blocoJsonString);
//                System.out.println("******************** BLOCO MINERADO RECEBIDO REENVIADO COM SUCESSO!");
//                System.out.println("*************************************************************************************");
//            } else {
//                //se é de outro: enviar para método de validação de blocos.
//                System.out.println("******************** BLOCO MINERADO RECEBIDO NÃO É PRÓPRIO...");
//                System.out.println("******************** ENVIANDO PARA VALIDAÇÃO...");
//                System.out.println("*************************************************************************************");
//                validaBlocos(bloco, blocoJsonString);
//            }
//            System.out.println("************* BLOCO MINERADO RECEBIDO: " + bloco);
//
//        } catch (JsonProcessingException e) {
//            System.out.println("*************************************************************************************");
//            System.out.println("************** ERRO COM BLOCO MINERADO RECEBIDO...REENVIANDO...");
//            System.out.println("*************************************************************************************");
//            rabbitTemplate.convertAndSend("bloco-minerado", blocoJsonString);
//            //throw new RuntimeException("Erro ao processar a PilaCoin minerada", e);
//        }
//    }
//
//    private void mineraBloco(String blocoStr) throws JsonProcessingException,
//                            NoSuchAlgorithmException, UnsupportedEncodingException {
////        Bloco bloco = getBloco(nodeBloco);
//        Boolean mineracaoBloco = Boolean.TRUE;
//        ObjectMapper objectMapper = new ObjectMapper();
//        //transforma a string do bloco em instância de Bloco.
//        Bloco bloco = objectMapper.readValue(blocoStr, Bloco.class);
//        parChaves = chaveService.leParChaves();
//        bloco.setChaveUsuarioMinerador(parChaves.getPublic().getEncoded());
//        bloco.setNomeUsuarioMinerador("ewerton-joaokunde");
//        DificuldadeJson dificuldadeJson = getDificuldade();
//        dificuldade = new BigInteger(dificuldadeJson.getDificuldade(), 16).abs();
//        System.out.println("******************************** INICIANDO MINERAÇÃO DO BLOCO...");
//        while (mineracaoBloco) {
//           //System.out.println("********************************************* MINERANDO...");
//            //setando o nonce para o bloco.
//            bloco.setNonce(getNonceBloco());
//            //transformando o bloco em String.
//            String blocoJson = new ObjectMapper().writeValueAsString(bloco);
//            MessageDigest md = MessageDigest.getInstance("sha-256");
//            //transformando a string do bloco em hash.
//            byte[] hash = md.digest(blocoJson.getBytes("UTF-8"));
//            BigInteger numHash = new BigInteger(hash).abs();
//            // Compara o hash gerado com a dificuldade
//            if (numHash.compareTo(dificuldade) < 0) {
//                System.out.println("******************* 1 BLOCO MINERADO! *************************");
//                ObjectMapper om = new ObjectMapper();
//                String blocoMineradoStr = om.writeValueAsString(bloco);
//                blocoService.save(bloco);
//                System.out.println("******************************* BLOCO SALVO COM SUCESSO! ENVIANDO PARA FILA...");
//                rabbitTemplate.convertAndSend("bloco-minerado", blocoMineradoStr);
//                System.out.println("******************************** BLOCO ENVIADO COM SUCESSO!");
//                mineracaoBloco = Boolean.FALSE;
//            }
//        }
//    }
//
//    private void validaBlocos(Bloco bloco, String blocoMineradoRecebido) {
//        try{
//            MessageDigest md = MessageDigest.getInstance("SHA-256");
//            BigInteger numHashBloco = new BigInteger(md.digest(blocoMineradoRecebido.getBytes(StandardCharsets.UTF_8))).abs();
//            byte[] hash = md.digest(blocoMineradoRecebido.getBytes(StandardCharsets.UTF_8));
//            DificuldadeJson dificuldadeJson = getDificuldade();
//            dificuldade = new BigInteger(dificuldadeJson.getDificuldade(), 16).abs();
//            if (numHashBloco.compareTo(dificuldade) < 0) {
//                System.out.println("******************* BLOCO VALIDADO COM SUCESSO!");
//                System.out.println("*************************************************************************************");
//                //assinar o pila e enviar para o server.
//                assinaBlocoValidado(bloco, hash);
//            } else {
//                System.out.println("******************* BLOCO NÃO VÁLIDO!");
//                System.out.println("******************* REENVIANDO PARA FILA DE BLOCOS MINERADOS...");
//                rabbitTemplate.convertAndSend("bloco-minerado", blocoMineradoRecebido);
//                System.out.println("******************* BLOCO REENVIADO COM SUCESSO!");
//                System.out.println("*************************************************************************************");
//            }
//        } catch (RuntimeException e) {
//            System.out.println("****************** ERRO AO VALIDAR O BLOCO!");
//            System.out.println("****************** REENVIANDO O BLOCO PARA A FILA 'bloco-minerado'");
//            rabbitTemplate.convertAndSend("bloco-minerado", blocoMineradoRecebido);
//            System.out.println("****************** BLOCO ENVIADO COM SUCESSO!");
//            System.out.println("*************************************************************************************");
//        } catch (NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException |
//                 NoSuchPaddingException | JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private void assinaBlocoValidado(Bloco blocoRecebido,
//                                    byte[] hash)
//            throws NoSuchPaddingException, NoSuchAlgorithmException,
//            InvalidKeyException, IllegalBlockSizeException, BadPaddingException, JsonProcessingException {
//        //populo o atributo assinaturaPilaCoin com a hash criptografada;
//        Cipher cipherRSA = Cipher.getInstance("RSA");
//        //iniciar o cipherRSA com o modo encriptografador;
//        KeyPair parChaves2 = chaveService.leParChaves();
//        cipherRSA.init(Cipher.ENCRYPT_MODE, parChaves2.getPrivate());
//        System.out.println("*************************************************************************************");
//        System.out.println("******************* ASSINANDO BLOCO VALIDADO...");
//        byte[] assinaturaBlocoJson = cipherRSA.doFinal(hash);
//        System.out.println("******************* BLOCO ASSINADO COM SUCESSO!");
//
//        //instanciando o blocoValidado para envio na fila bloco-validado.
//        BlocoValidado blocoValidado = BlocoValidado.builder()
//                .nomeValidador("ewerton-joaokunde")
//                .chavePublicaValidador(parChaves2.getPublic().getEncoded())
//                .assinaturaBloco(assinaturaBlocoJson)
//                .bloco(blocoRecebido).build();
//        System.out.println("****************** SALVANDO BLOCO VALIDADO EM BANCO...");
//        BlocoValidadoDto blocoValidadoDto = BlocoValidadoDto.builder()
//                .nomeValidador(blocoValidado.getNomeValidador())
//                .chavePublicaValidador(blocoValidado.getChavePublicaValidador().toString())
//                .assinaturaBloco(blocoValidado.getAssinaturaBloco().toString())
//                .build();
//        blocoValidadoDtoService.save(blocoValidadoDto);
//        //validacaoPilaJsonService.save(validacaoPilaJson);
//        System.out.println("****************** SALVO COM SUCESSO!");
//
//        System.out.println("****************** ENVIANDO BLOCO VALIDADO PARA FILA bloco-validado...");
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//
//        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
//        String blocoValidadoString = ow.writeValueAsString(blocoValidado);
//        rabbitTemplate.convertAndSend("bloco-validado", blocoValidadoString);
//        System.out.println("****************** BLOCO VALIDADO ENVIADO COM SUCESSO!");
//        System.out.println("*************************************************************************************");
//    }
//
//    private DificuldadeJson getDificuldade() {
//        return dificuldadeService.getLastDificuldade();
//    }
//
//    private String getNonceBloco() {
//        SecureRandom rnd = new SecureRandom();
//        return new BigInteger(128, rnd).toString();
//    }
//
//}