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

    private Long idQuery;
    private String usuario; //mesmo nomeUsuario da query original (de requisição);
    private List<PilaCoin> pilasResult;
    private List<Bloco> blocosResult;
    private List<Usuario> usuariosResult;
}
