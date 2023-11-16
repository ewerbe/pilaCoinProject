package br.com.ufsm.csi.pilacoin.service;

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
    private String queue_errors; //tratar se vierem erros

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private DificuldadeService dificuldadeService;
    @Autowired
    private PilaCoinService pilaCoinService;
    @Autowired
    private ValidacaoPilaJsonService validacaoPilaJsonService;
    @Autowired
    private PilaCoinJsonService pilaCoinJsonService;

    public static KeyPair parChaves = null;
    private static BigInteger dificuldade = BigInteger.ZERO;

    @RabbitListener(queues = {"${queue.dificuldade}"})
    public void receivePilaCoin(@Payload String strPilaCoinJson) {
        //System.out.println("Dificuldade: " + strPilaCoinJson);
        //pegar a dificuldade e passar para o PilaCoin usando objectmapper do jackson;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> retornoDificuldade = objectMapper.readValue(strPilaCoinJson,
                    new TypeReference<Map<String, Object>>() {});
            String dificuldadeStr = retornoDificuldade.get("dificuldade").toString();
            dificuldade = new BigInteger(dificuldadeStr, 16).abs();
            DificuldadeJson dificuldadePersistencia = new DificuldadeJson();
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

    @SneakyThrows
    private void minerarPilaCoin(BigInteger dificuldade, Boolean mineracaoAtiva) {

        ChaveService chaveService = new ChaveService();
        //PilaValidationService pilaValidationService = new PilaValidationService();

        //antes de minerar, vai pegar o par de chaves.
        parChaves = chaveService.leParChaves();
        if(mineracaoAtiva) {
            System.out.println("********** INICIANDO MINERAÇÃO DE PILACOINS");
        }
        while(mineracaoAtiva) {
            //pega a dificuldade;
            //cria a hash dos dados e compara com a dificuldade:
            //se for menor: registra o pilaCoin minerado.
            SecureRandom sr = new SecureRandom();
            BigInteger magicNumber = new BigInteger(128, sr);
//            byte[] bNum = new byte[256 / 8];
//            Random rnd = new Random(System.currentTimeMillis());
//            rnd.nextBytes(bNum);
//            pila.setNonce(new BigInteger(bNum).abs().toString());
            Random random = new Random();
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            byte[] byteArray = new byte[256 / 8];
            random.nextBytes(byteArray);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            //monta o objeto pilaCoin
            PilaCoin pilaCoin = PilaCoin.builder()
                    .chaveCriador(parChaves.getPublic().getEncoded())
                    .nomeCriador("ewerton-joaokunde")
                    .dataCriacao(new Date())
                    .nonce(new BigInteger(md.digest(byteArray)).abs().toString())
                    .build();
            //passa para json e depois cria a hash.
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            String pilaStringJson = ow.writeValueAsString(pilaCoin);
            //MessageDigest md = MessageDigest.getInstance("SHA-256");
            //byte[] hash = md.digest(pilaStringJson.getBytes("UTF-8"));
            BigInteger numHashPila = new BigInteger(md.digest(pilaStringJson.getBytes(StandardCharsets.UTF_8))).abs();
            //apenas valores positivos.
            //BigInteger numHashPila = new BigInteger(hash).abs();

            if (numHashPila.compareTo(dificuldade) < 0) {
                System.out.println("**************************** MINEROU 1 PILA!!");
                System.out.println("**************************** ENVIANDO PILA MINERADO...");
                System.out.println("**************************** SALVANDO PILA EM BANCO...");
                PilaCoinJson pilaCoinJson = PilaCoinJson
                        .builder()
                        .dataCriacao(pilaCoin.getDataCriacao())
                        .chaveCriador(pilaCoin.getChaveCriador().toString())
                        .nomeCriador(pilaCoin.getNomeCriador())
                        .nonce(pilaCoin.getNonce())
                        .build();
                pilaCoinJsonService.save(pilaCoinJson);
                System.out.println("**************************** SALVO COM SUCESSO!");
                rabbitTemplate.convertAndSend("pila-minerado", pilaStringJson);
                System.out.println("**************************** PILA ENVIADO COM SUCESSO!");
                //registraPila(pilaCoin , pilaCoin.getNonce());
            }
        }
    }

    //método redundante
//    private void registraPila(PilaCoin pilaCoin, String nonce) throws JsonProcessingException {
//        System.out.println("******** MONTANDO O PILA MINERADO...");
        //BigInteger bigIntegerNonce = new BigInteger(nonce);
//        private Long id;
//        private Date dataCriacao;
//        private byte[] chaveCriador;
//        private String nomeCriador;
//        private PilaCoin.StatusPila status;
//        private String nonce;
//        PilaCoinJson pilaJson = PilaCoinJson.builder()
//                .dataCriacao(pilaCoin.getDataHoraCriacao())
//                .chaveCriador(pilaCoin.getChavePublica())
//                .nomeCriador(pilaCoin.getNomeMinerador())
//                .nonce(nonce)
//                .build();
//        System.out.println("********************** pilaJson = " + pilaJson);
//        ObjectMapper objectMapper = new ObjectMapper();
//        String pilaJsonString = objectMapper.writeValueAsString(pilaJson);
//        System.out.println("************** PILACOIN MONTADO COM SUCESSO!");
//        System.out.println("************** ENVIANDO O PILACOIN...");

//        String strPilaCoinJsonDona = """
//        {"chaveCriador":"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAno9b8lIVrBP9J
//        l2+BZPCq18jlJEbiwOY6c3NOnSc9bvVlEzBy/z8Myfzlfq3YxuAR71Qq2UWiHTMiw307MPK/XY78kCdMXpEXQdrqu8J84GNuuaz/sZ
//        /74YADNdatDinuhxiDZd00ULBJodr6pGnB/LRjOTkM2DoHF7PLwzGYZj5TxMrPRW2DNbx/MBxK069mC+S1hyocbWTQlOaDt2zYiUfN
//        srf52ulmHM2DkrgCTjYn8nG2+OMSzNXkqN7sHJnW1E4rvdPyS2Bi6xn+OBO4jK2nOF8gu4qZCIJb2FOmlMH0D5lSqp9ZVqN/QNMtwg0
//        oyz90mDiUwBV8sSjmsrj/QIDAQAB","dataCriacao":1699124938360,"nomeCriador":"ADonato","nonce":"409507132086
//        43344792211455821639896881101755920158178356638536284138313"}
//        """;

//        rabbitTemplate.convertAndSend("pila-minerado", pilaJsonString);
////        rabbitTemplate.convertAndSend("pila-minerado", strPilaCoinJsonDona);
//        System.out.println("************** PILACOIN ENVIADO COM SUCESSO!");
//    }

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
            //PilaCoinJson pilaCoinJson = objectMapper.readValue(pilaJsonString, PilaCoinJson.class);
            PilaCoin pilaCoin = objectMapper.readValue(pilaJsonString, PilaCoin.class);
            //ver se o pila é próprio ou de outro minerador:
            System.out.println("********************* PILA MINERADO RECEBIDO!");
            if (pilaCoin.getNomeCriador().equals("ewerton-joaokunde")) {
                System.out.println("******************** PILA MINERADO RECEBIDO É PRÓPRIO!");
                //se é meu: reenviar pra fila "pila-minerado".
                System.out.println("******************** REENVIANDO PILA MINERADO RECEBIDO PARA FILA pila-minerado...");
                rabbitTemplate.convertAndSend("pila-minerado", pilaJsonString);
                System.out.println("******************** PILA MINERADO RECEBIDO REENVIADO COM SUCESSO!");
            } else {
                //se é de outro: enviar para método de validação de pilas.
                System.out.println("******************** PILA MINERADO RECEBIDO NÃO É PRÓPRIO...");
                System.out.println("******************** ENVIANDO PARA VALIDAÇÃO...");
                validaPilas(pilaCoin, pilaJsonString);
            }
            System.out.println("************* PilaCoin minerado recebido: " + pilaCoin);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao processar a PilaCoin minerada", e);
        }
    }

    //ativar esta audição da fila ewerton apenas depois de minerar algum pila, pois, senão, ela ainda não existe.
//    @RabbitListener(queues = "ewerton")
//    public void receiveMsgFeedbackUser(@Payload String feedback) {
//        try {
//            System.out.println("*************** Mensagem de feedback recebida: "+ feedback);
//        } catch (Exception e) {
//            throw new RuntimeException("Erro ao receber mensagem de feedback! ", e);
//        }
//    }

//    private void validaPilas(PilaCoinJson pilaCoinJson, String pilaMineradoRebecido) {
//        //fazer try catch: se qualquer erro ocorrer: reenviar o pilaMineradoRebecido para a fila "pila-minerado";
//        try{
//            //transformar o pila em hash e ver se é menor que a dificuldade;
//            PilaCoin pilaCoin = PilaCoin.builder()
//                    .chavePublica(pilaCoinJson.getChavePublica())
//                    .nomeMinerador(pilaCoinJson.getNomeMinerador())
//                    .dataHoraCriacao(pilaCoinJson.getDataHoraCriacao())
//                    .nonce(pilaCoinJson.getNonce())
//                    .build();
//            //passa para json e depois cria a hash.
//            ObjectMapper objectMapper = new ObjectMapper();
//            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//            String pilaJson = objectMapper.writeValueAsString(pilaCoin);
//            MessageDigest md = MessageDigest.getInstance("SHA-256");
//            byte[] hash = md.digest(pilaJson.getBytes("UTF-8"));
//            //apenas valores positivos.
//            BigInteger numHashPila = new BigInteger(hash).abs();
//            System.out.println("*************** VALIDANDO PILA...");
//            if (numHashPila.abs().compareTo(dificuldade) < 0) {
//                System.out.println("******************* PILA VALIDADO COM SUCESSO!");
//                assinaPilaValidado(pilaCoinJson, hash);
//            } else {
//                System.out.println("******************* PILA NÃO VÁLIDO!");
//                System.out.println("******************* REENVIANDO PARA FILA DE PILAS MINERADOS...");
//                rabbitTemplate.convertAndSend("pila-minerado", pilaMineradoRebecido);
//                System.out.println("******************* PILA REENVIADO COM SUCESSO!");
//            }
//        } catch (RuntimeException e) {
//            System.out.println("****************** ERRO AO VALIDAR O PILA!");
//            System.out.println("****************** REENVIANDO O PILA PARA A FILA 'pila-minerado'");
//            rabbitTemplate.convertAndSend("pila-minerado", pilaMineradoRebecido);
//            System.out.println("****************** PILA ENVIADO COM SUCESSO!");
//        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | JsonProcessingException |
//                 InvalidKeyException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
//            throw new RuntimeException(e);
//        }
//    }

    private void validaPilas(PilaCoin pilaCoin, String pilaMineradoRebecido) {
        //fazer try catch: se qualquer erro ocorrer: reenviar o pilaMineradoRebecido para a fila "pila-minerado";
        try{
            //transformar o pila em hash e ver se é menor que a dificuldade;
            Random random = new Random();
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            byte[] byteArray = new byte[256 / 8];
            random.nextBytes(byteArray);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // monta o objeto pilaCoin          TODO: já tem o objeto, não precisa montá-lo novamente.
//            PilaCoin pilaCoin = PilaCoin.builder()
//                    .chavePublica(pilaCoinJson.getChaveCriador())
//                    .nomeMinerador(pilaCoinJson.getNomeCriador())
//                    .dataHoraCriacao(pilaCoinJson.getDataCriacao())
//                    .nonce(pilaCoinJson.getNonce())
//                    .build();
            //passa para json e depois cria a hash.
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            String pilaJson = ow.writeValueAsString(pilaCoin);
            BigInteger numHashPila = new BigInteger(md.digest(pilaJson.getBytes(StandardCharsets.UTF_8))).abs();
            byte[] hash = md.digest(pilaJson.getBytes(StandardCharsets.UTF_8));
            if (numHashPila.compareTo(dificuldade) < 0) {
                System.out.println("******************* PILA VALIDADO COM SUCESSO!");
                //assinar o pila e enviar para o server.
                assinaPilaValidado(pilaCoin, hash);
            } else {
                System.out.println("******************* PILA NÃO VÁLIDO!");
                System.out.println("******************* REENVIANDO PARA FILA DE PILAS MINERADOS...");
                rabbitTemplate.convertAndSend("pila-minerado", pilaMineradoRebecido);
                System.out.println("******************* PILA REENVIADO COM SUCESSO!");
            }
        } catch (RuntimeException e) {
            System.out.println("****************** ERRO AO VALIDAR O PILA!");
            System.out.println("****************** REENVIANDO O PILA PARA A FILA 'pila-minerado'");
            rabbitTemplate.convertAndSend("pila-minerado", pilaMineradoRebecido);
            System.out.println("****************** PILA ENVIADO COM SUCESSO!");
        } catch (NoSuchAlgorithmException | JsonProcessingException |
                 InvalidKeyException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }


    private void assinaPilaValidado(PilaCoin pilaCoin,
                                    byte[] hash)
            throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        //se for menor: instancia o objeto validacaoPilaJson e popula com os atributos do pila;
        //para assinar o pila validado: pego a hash do pila e criptografo com a minha chave privada;
        //populo o atributo assinaturaPilaCoin com a hash criptografada;
        Cipher cipherRSA = Cipher.getInstance("RSA");
        //iniciar o cipherRSA com o modo encriptografador;
        cipherRSA.init(Cipher.ENCRYPT_MODE, parChaves.getPrivate());
        //finalizar a criptografia com o que se quer criptografar.
        System.out.println("******************* ASSINANDO PILACOIN VALIDADO...");
        byte[] assinaturaPilaCoinJson = cipherRSA.doFinal(hash);
        System.out.println("******************* PILACOIN ASSINADO COM SUCESSO!");
        //instanciando o pilacoin validado para envio na fila pila-validado.
        ValidacaoPilaJson validacaoPilaJson = ValidacaoPilaJson.builder()
                .nomeValidador("ewerton-joaokunde")
                .chavePublicaValidador(parChaves.getPublic().getEncoded())
                .assinaturaPilaCoin(assinaturaPilaCoinJson)
                .pilaCoin(pilaCoin).build();
        System.out.println("****************** SALVANDO PILA VALIDADO EM BANCO...");
        validacaoPilaJsonService.save(validacaoPilaJson);
        System.out.println("****************** SALVO COM SUCESSO!");
        System.out.println("****************** ENVIANDO PILACOIN VALIDADO PARA FILA pila-validado...");
        rabbitTemplate.convertAndSend("pila-validado", validacaoPilaJson);
        System.out.println("****************** PILACOIN VALIDADO ENVIADO COM SUCESSO!");
    }

}
