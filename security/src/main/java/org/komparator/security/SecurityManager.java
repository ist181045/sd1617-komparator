package org.komparator.security;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.HashSet;
import java.util.Set;

import pt.ulisboa.tecnico.sdis.cert.CertUtil;

public class SecurityManager {
    private static final int MSG_TIMEOUT = 3;
    private static final String KEY_PASSWORD = "M6cggAUT";

    private static SecurityManager theManager;

    private String sender;
    private String destination;
    private Set<String> tokens;

    private SecurityManager() {
        this.sender = null;
        this.destination = null;
        this.tokens = new HashSet<>();
    }

    public static SecurityManager getInstance() {
        if (theManager == null)
            theManager = new SecurityManager();
        return theManager;
    }

    public static int getMsgTimeout() {
        return MSG_TIMEOUT;
    }

    public String getDestination() {
    	return destination;
    }

    public void setDestination(String destination) {
    	this.destination = destination;
    }

    public String getSender() {
    	return sender;
    }

    public void setSender(String sender) {
    	this.sender = sender;
    }

    public String getPassword() {
    	return KEY_PASSWORD;
    }

    public Set<String> getTokens() {
    	return tokens;
    }

    public PublicKey getPublicKey(String entity) {
        try {
            Certificate cert = CertUtil.getX509CertificateFromResource(
                    entity + ".cer");

            return CertUtil.getPublicKeyFromCertificate(cert);
        } catch (IOException ioe) {
            System.err.println("I/O error: " + ioe.getMessage());
        } catch (CertificateException ce) {
            System.err.println("Error retrieving certificate: "
                    + ce.getMessage());
        }

        return null;
    }

    public PrivateKey getPrivateKey(String entity) {
        try {
            KeyStore keyStore = CertUtil.readKeystoreFromResource(
                    entity + ".jks",
                    getPassword().toCharArray());

            return CertUtil.getPrivateKeyFromKeyStore(entity.toLowerCase(),
                    getPassword().toCharArray(), keyStore);
        } catch (KeyStoreException kse) {
            System.err.println("Error loading keystore: " + kse.getMessage());
        } catch (UnrecoverableKeyException uke) {
            System.err.println("Couldn't recover key: " + uke.getMessage());
        }

        return null;
    }
}
