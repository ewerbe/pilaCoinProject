package br.com.ufsm.csi.pilacoin.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder(alphabetic = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
public class Usuario {

    @Id
    @GeneratedValue
    private Long id;
    @Column(unique = true)
    private byte[] chavePublica;
    @Column(unique = true)
    private String nome;
//    @OneToMany(mappedBy = "usuariosResult")
//    private Collection<QueryResposta> queryResposta;
//
//    public Collection<QueryResposta> getQueryResposta() {
//        return queryResposta;
//    }
//
//    public void setQueryResposta(Collection<QueryResposta> queryResposta) {
//        this.queryResposta = queryResposta;
//    }
}
