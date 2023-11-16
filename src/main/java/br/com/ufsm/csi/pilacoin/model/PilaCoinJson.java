package br.com.ufsm.csi.pilacoin.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder(alphabetic = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
public class PilaCoinJson implements Cloneable{

    @Id
    @GeneratedValue
    private Long id;
    private Date dataCriacao;
    private String chaveCriador;
    private String nomeCriador;
    private StatusPila status;
    private String nonce;

    public enum StatusPila{AG_BLOCO, AG_VALIDACAO, BLOCO_EM_VALIDACAO, VALIDO, INVALIDO}

    @Override
    public PilaCoinJson clone() {
        try{
            PilaCoinJson clone = (PilaCoinJson) super.clone();
            return clone();
        }catch (CloneNotSupportedException e){
            throw new AssertionError();
        }
    }
}
