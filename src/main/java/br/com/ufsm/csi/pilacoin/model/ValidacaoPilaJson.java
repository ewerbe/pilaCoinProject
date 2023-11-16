package br.com.ufsm.csi.pilacoin.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder(alphabetic = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
public class ValidacaoPilaJson {
    @Id
    @SequenceGenerator(name = "seq_validacao_pilacoin", sequenceName = "seq_validacao_pilacoin", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_validacao_pilacoin")
    @JsonIgnore
    private String nomeValidador;
    private byte[] chavePublicaValidador;
    private byte[] assinaturaPilaCoin;
    @OneToOne
    @JoinColumn(name = "id_pilacoin")
    private PilaCoin pilaCoin;
}
