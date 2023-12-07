package br.com.ufsm.csi.pilacoin.service;

import br.com.ufsm.csi.pilacoin.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import jakarta.annotation.PostConstruct;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TransferenciaValidationService {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private PilaCoinService pilaCoinService;
    @Autowired
    private ValidacaoPilaJsonService validacaoPilaJsonService;
    @Autowired
    private ChaveService chaveService;
    @Autowired
    private TransacaoService transacaoService;

    //query de requisição da listagem de usuários, blocos ou pilas;
    public void enviaQuery(Query query) throws JsonProcessingException {
        //transforma o objeto query pra string e envia pra fila;
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String queryString = ow.writeValueAsString(query);
        System.out.println("************************* ENVIANDO REQUISIÇÃO DE QUERY...");
        rabbitTemplate.convertAndSend("query", queryString);
    }

    //recebe as queries de respostas das requisições enviadas pro server.
    @RabbitListener(queues = "ewerton-joaokunde-query")
    public void recebeQueryResposta(@Payload String queryRespostaString) {
        try {
            System.out.println("***************************** RECEBIDA QUERY RESPOSTA...");
//            System.out.println("***************************** QUERY RECEBIDA DE RESPOSTA: " + queryResposta);
            //se é USUARIOS envia para tratamento de usuarios;
            ObjectMapper objectMapper = new ObjectMapper();
            QueryResposta queryResposta = objectMapper
                    .readValue(queryRespostaString, QueryResposta.class);
            //idQuery para query de listagem de usuários.
            //System.out.println("************************************************ queryResposta = " + queryRespostaString);
            if(queryResposta.getUsuariosResult() != null && !queryResposta.getUsuariosResult().isEmpty()) {
                salvaListaUsuarios(queryResposta);
            } else if (queryResposta.getPilasResult() != null && !queryResposta.getPilasResult().isEmpty()) {
                salvaMeusPilaCoinsValidados(queryResposta);
            }
            else {
                System.out.println("************************************** NÃO ENTROU EM NADA!");
            }
//            String retornoQuery = nodeQueryResposta.get("blocosResult").toString();
//            System.out.println("***************************** RECEBIDA QUERY RESPOSTA...");
//            if(retornoQuery.isEmpty()) {
//                System.out.println("************************ RECEBIDA QUERY USUARIOS *********");
//
//            } else {
//                System.out.println("************************ RECEBIDA QUERY BLOCOS *********");
//                salvaListaBlocos(queryResposta);
//            }
            //se é BLOCOS envia para tratamento de blocos.
            //System.out.println("*************** Query resposta recebida: "+ queryResposta);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao receber query resposta! ", e);
        }
    }

    //requisita a lista de usuários e a lista de blocos para persisti-las na base de dados.
//    @PostConstruct
//    private void buscaListagens() throws JsonProcessingException {
//        //monta a query do tipo USUARIOS e envia através do método enviarQuery(queryUsuarios);
//        System.out.println("************************************* BUSCANDO LISTAGENS...");
//        Query queryUsuarios = getQueryUsuarios();
//        System.out.println("************************************* BUSCANDO LISTAGENS DE USUARIOS...");
//        enviaQuery(queryUsuarios);
//        //monta a query do tipo BLOCOS e envia através do método enviarQuery(queryBlocos);
//        Query queryBlocos = getQueryBlocos();
//        System.out.println("************************************* BUSCANDO LISTAGENS DE BLOCOS...");
//        enviaQuery(queryBlocos);
//    }

    @PostConstruct
    private void testaTransferenciaPilaCoin() throws JsonProcessingException, NoSuchPaddingException,
                                                IllegalBlockSizeException, NoSuchAlgorithmException,
                                                BadPaddingException, InvalidKeyException {
        //monta um objeto usuarioDestino para receber a transferencia e instancia um pila válido para ser transferido
        System.out.println("************************************* TESTANDO TRANSFERÊNCIA DE PILACOIN...");
        List<Usuario> listaUsuarios = usuarioService.findAll();
        Usuario usuarioDestino = listaUsuarios.get(4);
        //TODO: pegar um pila meu válido do banco (popular o banco antes); *********************************
        Optional<PilaCoin> pilaCoin = pilaCoinService.findById(65L);
        tranferePilaCoin(pilaCoin, usuarioDestino);
    }

    //requisita os meus pilaCoins validados do servidor para jogar pro banco.
//    @PostConstruct
//    private void buscaMeusPilaCoinsValidados() throws JsonProcessingException {
//        System.out.println("************************************* BUSCANDO MEUS PILACOINS...");
//        //monta a query do tipo PILAS e envia através do método enviarQuery(queryBlocos);
//        Query queryPilas = getQueryPilas();
//        enviaQuery(queryPilas);
//    }

    private Query getQueryPilas() {
        return Query.builder()
                .idQuery(13667L)
                .nomeUsuario("ewerton-joaokunde")
                .tipoQuery(Query.TypeQuery.PILA)
                .build();
    }

    private Query getQueryUsuarios() {
        return Query.builder()
                .idQuery(1340L)
                .nomeUsuario("ewerton-joaokunde")
                .tipoQuery(Query.TypeQuery.USUARIOS)
                .build();
    }

    private Query getQueryBlocos() {
        return Query.builder()
                .idQuery(1341L)
                .nomeUsuario("ewerton-joaokunde")
                .tipoQuery(Query.TypeQuery.BLOCO)
                .build();
    }

    private void salvaListaUsuarios(QueryResposta queryRespostaUsuarios) throws JsonProcessingException {
        //trata a lista de usuarios e envia pro banco;
        System.out.println("*************************************** SALVANDO LISTA DE USUÁRIOS...");
        List<Usuario> usuariosList = queryRespostaUsuarios.getUsuariosResult();
        usuarioService.saveAll(usuariosList);
        System.out.println("*************************************** LISTA DE USUÁRIOS SALVA COM SUCESSO!");
    }

    private void salvaMeusPilaCoinsValidados(QueryResposta queryRespostaPilas) throws JsonProcessingException {
        //trata a lista de pilaCoins e envia pro banco;
        System.out.println("*************************************** SALVANDO LISTA DE MEUS PILACOINS VÁLIDOS...");
        List<PilaCoin> pilaCoinList = queryRespostaPilas.getPilasResult();
        for(PilaCoin pila : pilaCoinList) {
            if(pila.getNomeCriador().equals("ewerton-joaokunde") && pila.getStatus().equals("VALIDO")) {
                System.out.println("*********************************** SALVANDO PILA VÁLIDO PRÓPRIO NO BANCO...");
                pilaCoinService.save(pila);
                System.out.println("*********************************** PILA VÁLIDO PRÓPRIO SALVO COM SUCESSO!");
            }
        }
        System.out.println("******************************************* ACABOU DE SALVAR OS PILA VÁLIDOS PRÓPRIOS NO BANCO!");
    }

    private void salvaListaBlocos(String queryRespostaBlocos) {
        //trata a lista de blocos e envia pro banco;
        System.out.println("********************************* queryRespostaBlocos = " + queryRespostaBlocos);
    }

    //transferir pilacoin;
    public void tranferePilaCoin(Optional<PilaCoin> pilaCoin, Usuario usuarioDestino) throws
                                JsonProcessingException, NoSuchPaddingException, IllegalBlockSizeException,
                                NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        //recebe o meu pila para ser transferido para o usuario tbm recebido.
        //instancia o objeto transacao
        KeyPair parChaves = chaveService.leParChaves();
        //fazer a assinatura pra inserir no objeto transacaoPilaCoin.
        Transacao transacaoPilaCoin = Transacao
                .builder()
                .chaveUsuarioOrigem(parChaves.getPublic().getEncoded())
                .chaveUsuarioDestino(usuarioDestino.getChavePublica())
                .noncePila(pilaCoin.get().getNonce())
                .dataTransacao(new Date())
                .build();
        byte[] assinaturaPilaTransferencia = assinaPilaCoinTransferencia(transacaoPilaCoin);
        transacaoPilaCoin.setAssinatura(assinaturaPilaTransferencia);
        System.out.println("******************************************* TRANSACAO ASSINADA COM SUCESSO!");
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String transacaoPilaCoinString = ow.writeValueAsString(transacaoPilaCoin);
        transacaoService.save(transacaoPilaCoin);
        System.out.println("****************************** TRANSACAO SALVA EM BANCO E ENVIADA PARA FILA!");
        rabbitTemplate.convertAndSend("transferir-pila", transacaoPilaCoinString);
    }

    private byte[] assinaPilaCoinTransferencia(Transacao transacaoPilaCoin) throws NoSuchAlgorithmException,
                                                NoSuchPaddingException, JsonProcessingException, InvalidKeyException,
                                                IllegalBlockSizeException, BadPaddingException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String transacaoPilaCoinString = ow.writeValueAsString(transacaoPilaCoin);
        byte[] hash = md.digest(transacaoPilaCoinString.getBytes(StandardCharsets.UTF_8));
        Cipher cipherRSA = Cipher.getInstance("RSA");
        //iniciar o cipherRSA com o modo encriptografador;
        KeyPair parChaves2 = chaveService.leParChaves();
        cipherRSA.init(Cipher.ENCRYPT_MODE, parChaves2.getPrivate());
        System.out.println("*************************** ASSINANDO TRANSACAO DE PILA...");
        return cipherRSA.doFinal(hash);
    }

}
