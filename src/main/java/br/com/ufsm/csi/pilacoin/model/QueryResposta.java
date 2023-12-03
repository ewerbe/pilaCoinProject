package br.com.ufsm.csi.pilacoin.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder(alphabetic = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QueryResposta {

    //@Id
    private Long idQuery;
    private String usuario; //mesmo nomeUsuario da query original (de requisição);
//    @ManyToMany
//    @JoinTable(
//            name = "query_resposta_blocos",
//            joinColumns = @JoinColumn(name = "id_query"),
//            inverseJoinColumns = @JoinColumn(name = "id_bloco")
//    )
    private List<Bloco> blocosResult;

//    @ManyToMany
//    @JoinTable(
//            name = "query_resposta_usuarios",
//            joinColumns = @JoinColumn(name = "id_query"),
//            inverseJoinColumns = @JoinColumn(name = "id_usuario")
//    )
    private List<Usuario> usuariosResult;

}
