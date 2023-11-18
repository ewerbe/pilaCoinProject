package br.com.ufsm.csi.pilacoin.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
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
@Entity
public class Bloco {
    @Id
    @JsonIgnore
    private Long numeroBloco;
    private String nonceBlocoAnterior;
    private String nonce;
    private String chaveUsuarioMinerador; //chave-pública do usuário que descobreo nonce;
    @OneToMany
    @JoinColumn(name = "id_bloco")
    private List<Transacao> transacoes;
}
