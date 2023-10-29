package br.com.ufsm.csi.pilacoin.service;

import br.com.ufsm.csi.pilacoin.repository.PilaCoinRepository;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.KeyPair;

@Service
public class MineradoraService {

    @Autowired
    private PilaCoinRepository pilaCoinRepository;

    public static BigInteger dificuldade = BigInteger.ZERO;
    //public static boolean mineiracaoAtiva = false;

    public static KeyPair parChaves;

    @SneakyThrows
    public void minerarPilaCoin() {

        //antes de minerar, vai pegar o par de chaves.

    }
}
