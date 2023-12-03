package br.com.ufsm.csi.pilacoin.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder(alphabetic = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Query {

    private Long idQuery;
    private String nomeUsuario; //nome do usuário para saber qual a fila de resposta.
    private TypeQuery tipoQuery; //"USUARIOS", "PILA" OU "BLOCOS"
    private String status; //não obrigatório. pode ser usado para filtrar os resultados por status do pila ou do bloco.
    private String usuarioMinerador; //não obrigatório. pode ser usado para filtrar os resultados por nome de usuário;
    private String nonce; //não obrigatório. pode ser usado para filtrar os resultados pelo nonce;
    private Long idBloco; //não obrigatório. pode ser usado para filtrar os resultados pelo seu número.

    public enum TypeQuery {
        USUARIOS, PILA, BLOCO
    }
}
