package br.com.ufsm.csi.pilacoin.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
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
public class DificuldadeJson {

    @Id
    @SequenceGenerator(name = "dificuldade_json_seq", sequenceName = "dificuldade_json_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dificuldade_json_seq")
    @JsonIgnore
    private Long dificuldade_json_seq;
    private String dificuldade;
    private String inicio;
    private String validadeFinal;
}
