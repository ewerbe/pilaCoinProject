package br.com.ufsm.csi.pilacoin.service;

import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@Service
public class ChaveService {

    //retorna o par de chave pública e privada
    @SneakyThrows
    public KeyPair leParChaves() {
        File fpub = new File("public.key");
        File fpriv = new File("private.key");
        if (fpub.exists() && fpriv.exists()) {
            FileInputStream pubIn = new FileInputStream(fpub);
            FileInputStream privIn = new FileInputStream(fpriv);
            byte[] bArrayPub = new byte[(int) pubIn.getChannel().size()];
            byte[] bArrayPriv = new byte[(int) privIn.getChannel().size()];
            pubIn.read(bArrayPub);
            privIn.read(bArrayPriv);
            PublicKey chavePublica = KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(bArrayPub));
            PrivateKey chavePrivada = KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(bArrayPriv));
            return new KeyPair(chavePublica, chavePrivada);
        } else {
            KeyPairGenerator geradorKeyPair= KeyPairGenerator.getInstance("RSA");
            geradorKeyPair.initialize(2048);
            KeyPair keyPair = geradorKeyPair.generateKeyPair();
            FileOutputStream filePubOut = new FileOutputStream("public.key", false);
            FileOutputStream filePrivOut = new FileOutputStream("private.key", false);
            filePubOut.write(keyPair.getPublic().getEncoded());
            filePrivOut.write(keyPair.getPrivate().getEncoded());
            filePubOut.close();
            filePrivOut.close();
            return keyPair;
        }
    }
}
