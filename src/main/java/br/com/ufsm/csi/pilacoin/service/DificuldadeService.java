package br.com.ufsm.csi.pilacoin.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class DificuldadeService {

    private void divulgaDificuldade() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] bArray = new byte[267/8];
        secureRandom.nextBytes(bArray);
    }
}
