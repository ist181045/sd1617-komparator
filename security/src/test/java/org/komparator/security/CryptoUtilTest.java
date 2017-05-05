package org.komparator.security;

import static javax.xml.bind.DatatypeConverter.printHexBinary;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import pt.ulisboa.tecnico.sdis.cert.CertUtil;

public class CryptoUtilTest {

    // static fields
    static final String CERTIFICATE = "example.cer";

    static final String KEYSTORE = "example.jks";
    static final String KEYSTORE_PASSWORD = "1nsecure";

    static final String KEY_ALIAS = "example";
    static final String KEY_PASSWORD = "ins3cur3";


    static boolean outputFlag = CertUtil.outputFlag;
    static PublicKey publicKey;
    static PrivateKey privateKey;

    // one-time initialization and clean-up
    @BeforeClass
    public static void oneTimeSetUp() throws IOException, CertificateException,
            UnrecoverableKeyException, KeyStoreException {
        CertUtil.outputFlag = true; // Print some stuff

        Certificate cert = CertUtil.getX509CertificateFromResource(CERTIFICATE);

        publicKey = cert.getPublicKey();
        privateKey = CertUtil.getPrivateKeyFromKeyStoreResource(KEYSTORE,
                KEYSTORE_PASSWORD.toCharArray(), KEY_ALIAS,
                KEY_PASSWORD.toCharArray());
    }

    // members
    final String plainText = "Look at me, mah!";
    final byte[] plainBytes = plainText.getBytes();

    // tests
    @Test
    public void testEncryptionWithPrivateKey() {
        System.out.printf("Encrypting text using private key '%s' in keystore '%s'%n",
                KEY_ALIAS, KEYSTORE);
        System.out.println("Text: " + plainText);
        System.out.println("Bytes: " + printHexBinary(plainBytes));

        System.out.println("Attempting to encrypt..");
        byte[] encrypted = CryptoUtil.asymCipher(privateKey, plainBytes);
        assertNotNull(encrypted);
        System.out.printf("Encrypted: %s...%n",
                printHexBinary(encrypted).substring(0, 32));

        System.out.println("Attempting to decrypt..");
        byte[] decrypted = CryptoUtil.asymDecipher(publicKey, encrypted);
        assertNotNull(decrypted);
        System.out.println("Decrypted: " + printHexBinary(decrypted));

        System.out.print("Assuring the data's the same.. ");
        assertEquals(new String(plainBytes), new String(decrypted));
        System.out.println("OK");
    }

    @Test
    public void testEncryptionWithPublicKey() {
        System.out.printf("Encrypting text using public key from X509 Certificate '%s'%n",
                CERTIFICATE);
        System.out.println("Text: " + plainText);
        System.out.println("Bytes: " + printHexBinary(plainBytes));

        System.out.println("Attempting to encrypt..");
        byte[] encrypted = CryptoUtil.asymCipher(publicKey, plainBytes);
        assertNotNull(encrypted);
        System.out.printf("Encrypted: %s..%n",
                printHexBinary(encrypted).substring(0, 32));

        System.out.println("Attempting to decrypt..");
        byte[] decrypted = CryptoUtil.asymDecipher(privateKey, encrypted);
        assertNotNull(decrypted);
        System.out.println("Decrypted: " + printHexBinary(decrypted));

        System.out.print("Assuring the data's the same.. ");
        assertEquals(new String(plainBytes), new String(decrypted));
        System.out.println("OK");
    }

    @Test
    public void testWithWrongKey() {
        // Attempt encryption and decryption with wrong key pair (i.e. same key
        // for both cases)

        System.out.println("Encrypting and decrypting with private key.. ");
        {
            byte[] encrypted = CryptoUtil.asymCipher(privateKey, plainBytes);
            byte[] decrypted = CryptoUtil.asymDecipher(privateKey, encrypted);
            assertNull(decrypted);
        }
        System.out.println("public OK");

        System.out.println("Encrypting and decrypting with public key.. ");
        {
            byte[] encrypted = CryptoUtil.asymCipher(publicKey, plainBytes);
            byte[] decrypted = CryptoUtil.asymDecipher(publicKey, encrypted);
            assertNull(decrypted);
        }
        System.out.println("private OK");
    }

    @AfterClass
    public static void oneTimeTearDown() {
        CertUtil.outputFlag = outputFlag;
    }

}
