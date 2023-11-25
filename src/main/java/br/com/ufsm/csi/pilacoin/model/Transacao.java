package br.com.ufsm.csi.pilacoin.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder(alphabetic = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
public class Transacao {

    @Id
    @JsonIgnore
    private Long id;
    private byte[] chaveUsuarioOrigem;
    private byte[] chaveUsuarioDestino;
    private byte[] assinatura;
    private String noncePila;
    private Date dataTransacao;
    private String Status;
    @ManyToOne
    @JoinColumn(name = "id_bloco")
    private Bloco bloco;
}