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
    private static final String UDDI_GROUPID = "A58".toLowerCase();
    private static final String UDDI_PASSWORD = "M6cggAUT";
    private static final String UDDI_URL =
            "http://" + UDDI_GROUPID + ":" + UDDI_PASSWORD
                      + "@uddi.sd.rnl.tecnico.ulisboa.pt/9090";

    private static final String CA_URL =
            "http://sec.sd.rnl.tecnico.ulisboa.pt:8081/ca";

    private static final int MSG_TIMEOUT = 3;

    private static SecurityManager theManager;

    private String sender;

    private String receiver;
    private Set<String> tokens;

    public static SecurityManager getInstance() {
        if (theManager == null)
            theManager = new SecurityManager();
        return theManager;
    }

    public static int getMsgTimeout() {
        return MSG_TIMEOUT;
    }

    public static String getUddiGroupid() {
        return UDDI_GROUPID;
    }

    public static String getUddiPassword() {
        return UDDI_PASSWORD;
    }

    public static String getUddiUrl() {
        return UDDI_URL;
    }

    public static String getCaUrl() {
        return CA_URL;
    }

    private SecurityManager() {
        this.sender = null;
        this.receiver = null;
        this.tokens = new HashSet<>();
    }

    public String getReceiver() {
    	return receiver;
    }

    public void setReceiver(String receiver) {
    	this.receiver = receiver;
    }

    public String getSender() {
    	return sender;
    }

    public void setSender(String sender) {
    	this.sender = sender;
    }

    public String getPassword() {
    	return UDDI_PASSWORD;
    }

    public Set<String> getTokens() {
    	return tokens;
    }

    public synchronized boolean addToken(String token) {
        return tokens.add(token);
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
