package br.com.ufsm.csi.pilacoin.service;

import br.com.ufsm.csi.pilacoin.model.Query;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import jakarta.annotation.PostConstruct;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class TransferenciaValidationService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

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
    public void recebeQueryResposta(@Payload String queryResposta) {
        try {
            System.out.println("***************************** RECEBIDA QUERY RESPOSTA...");
            System.out.println("***************************** QUERY RECEBIDA DE RESPOSTA: " + queryResposta);
            //se é USUARIOS envia para tratamento de usuarios;
            //transforma pra nodeJson pra pegar a propriedade tipoQuery;
            ObjectMapper mapper = new ObjectMapper();
            JsonNode nodeQueryResposta = mapper.readTree(queryResposta);
            String retornoQuery = nodeQueryResposta.get("blocosResult").toString();
            System.out.println("***************************** RECEBIDA QUERY RESPOSTA...");
            if(retornoQuery.isEmpty()) {
                System.out.println("************************ RECEBIDA QUERY USUARIOS *********");
                salvaListaUsuarios(queryResposta);
            } else {
                System.out.println("************************ RECEBIDA QUERY BLOCOS *********");
                salvaListaBlocos(queryResposta);
            }
            //se é BLOCOS envia para tratamento de blocos.
            System.out.println("*************** Query resposta recebida: "+ queryResposta);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao receber query resposta! ", e);
        }
    }

    //requisita a lista de usuários e a lista de blocos para persisti-las na base de dados.
    @PostConstruct
    private void buscaListagens() throws JsonProcessingException {
        //monta a query do tipo USUARIOS e envia através do método enviarQuery(queryUsuarios);
        System.out.println("************************************* BUSCANDO LISTAGENS...");
        Query queryUsuarios = getQueryUsuarios();
        System.out.println("************************************* BUSCANDO LISTAGENS DE USUARIOS...");
        enviaQuery(queryUsuarios);
        //monta a query do tipo BLOCOS e envia através do método enviarQuery(queryBlocos);
        Query queryBlocos = getQueryBlocos();
        System.out.println("************************************* BUSCANDO LISTAGENS DE BLOCOS...");
        enviaQuery(queryBlocos);
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

    private void salvaListaUsuarios(String queryRespostaUsuarios) {
        //trata a lista de usuarios e envia pro banco;
        //imprimir o que veio na lista pra ver como tá
        System.out.println("********************************* queryRespostaUsuarios = " + queryRespostaUsuarios);
    }

    private void salvaListaBlocos(String queryRespostaBlocos) {
        //trata a lista de blocos e envia pro banco;
        System.out.println("********************************* queryRespostaBlocos = " + queryRespostaBlocos);
    }
    //transferir pilacoin;


}
