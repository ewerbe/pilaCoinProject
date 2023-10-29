package br.com.ufsm.csi.pilacoin.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
public class PilaCoinJson implements Cloneable{

    private Long id;
    private Date dataCriacao;
    private byte[] chaveCriador;
    private String nomeCriador;
    private PilaCoin.StatusPila status;
    private String nonce;

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
