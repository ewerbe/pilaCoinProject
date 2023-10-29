package br.com.ufsm.csi.pilacoin.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HexFormat;

@Data
@Builder
@JsonPropertyOrder(alphabetic = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PilaCoin {

    private Date dataCriacao;
    private byte[] chaveCriador;
    private String nomeCriador;
    private byte[] nonce;     //big integer de 128 bits


    public enum StatusPila{AG_BLOCO, AG_VALIDACAO, BLOCO_EM_VALIDACAO, VALIDO, INVALIDO}

    //@SneakyThrows
//    public static void main(String[] args) throws NoSuchAlgorithmException {
//
//        //salvar a chave publica e privada e recuperar
//        byte[] pubKey = null;
//        //criar o par de chaves;
//        //instanciar o pilaCoin com seus atributos;
//        //gerar o hash do pilaCoin para comparação com a dificuldade.
//        PilaCoin pilacoin = PilaCoin.builder().chavePublica(pubKey)
//                .dataHoraCriacao(new Date()).nomeMinerador("Ewerton").build();
//        //salvar a chave pública e privada e recuperar
//        byte[] valorDif = HexFormat.of().parseHex("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");
//        BigInteger dificuldade = new BigInteger(valorDif);
//        BigInteger hash;
//        MessageDigest md = MessageDigest.getInstance("SHA-256");
//
//        do{
//            String json = "";
//            hash = new BigInteger((md.digest(json.getBytes(StandardCharsets.UTF_8))));
//            hash = hash.abs();
//
//        } while (hash.compareTo(dificuldade) < 0);
//            //quando sair deste laço é pq achou.
//
//
//    }

    public void montaPilaMinerado() {
        //monta o objeto e envia pro server em método específico na classe PilaValidationService;
    }

    //    {
//        BigInteger dificuldade = new BigInteger("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff", 16).abs();
//    }
}
