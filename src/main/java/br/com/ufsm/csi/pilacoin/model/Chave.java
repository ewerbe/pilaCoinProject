package br.com.ufsm.csi.pilacoin.model;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Chave {

    private byte[] chavePublica;
    private byte[] chavePrivada;
}
