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

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.MessageDigest;
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

    @RabbitListener(queues = {"${queue.dificuldade}"})
    public void receivePilaCoin(@Payload String strPilaCoinJson) {
        //System.out.println("Dificuldade: " + strPilaCoinJson);
        //pegar a dificuldade e passar para o PilaCoin usando objectmapper do jackson;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> retornoDificuldade = objectMapper.readValue(strPilaCoinJson,
                    new TypeReference<Map<String, Object>>() {});
            String dificuldadeStr = retornoDificuldade.get("dificuldade").toString();
            BigInteger dificuldade = new BigInteger(dificuldadeStr, 16);

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
            // Processar a mensagem recebida da fila "pila-minerado"
            ObjectMapper objectMapper = new ObjectMapper();
            PilaCoinJson pilaCoinJson = objectMapper.readValue(pilaJsonString, PilaCoinJson.class);

            // Faça o que você deseja com a PilaCoin minerada, por exemplo, registrá-la no sistema ou realizar alguma validação.
            System.out.println("PilaCoin minerada recebida: " + pilaCoinJson);

            // Implemente o código aqui para lidar com a PilaCoin minerada, como salvá-la no banco de dados, atualizar estatísticas, etc.

        } catch (JsonProcessingException e) {
            // Trate exceções aqui, como problemas de desserialização do JSON.
            throw new RuntimeException("Erro ao processar a PilaCoin minerada", e);
        }
    }

}
