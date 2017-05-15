package org.komparator.security;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.HashSet;
import java.util.Set;

import pt.ulisboa.tecnico.sdis.cert.CertUtil;
import pt.ulisboa.tecnico.sdis.ws.cli.CAClient;
import pt.ulisboa.tecnico.sdis.ws.cli.CAClientException;

public class SecurityManager {
    private static final String UDDI_GROUP_ID = "a58";
    private static final String UDDI_PASSWORD = "M6cggAUT";
    private static final String UDDI_URL =
            "http://" + UDDI_GROUP_ID + ":" + UDDI_PASSWORD
                      + "@uddi.sd.rnl.tecnico.ulisboa.pt/9090";

    private static final String CA_CERT = "ca.cer";
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

    public static String getUddiGroupId() {
        return UDDI_GROUP_ID;
    }

    public static String getUddiPassword() {
        return UDDI_PASSWORD;
    }

    public static String getUddiUrl() {
        return UDDI_URL;
    }

    public static String getCaCert() {
        return CA_CERT;
    }

    public static String getCaUrl() {
        return CA_URL;
    }

    public static PublicKey getPublicKey(String entity) {
        try {
            CAClient cac = new CAClient(getUddiUrl(), entity);
            String certString = cac.getCertificate(entity);

            byte[] bytes = certString.getBytes(StandardCharsets.UTF_8);
            InputStream in = new ByteArrayInputStream(bytes);
            CertificateFactory certFactory =
                    CertificateFactory.getInstance("X.509");
            Certificate cert = certFactory.generateCertificate(in);

            if(CertUtil.verifySignedCertificate(
                    cert, CertUtil.getX509CertificateFromResource(CA_CERT))) {
                return CertUtil.getPublicKeyFromCertificate(cert);
            }
        } catch (IOException ioe) {
            System.err.println("I/O error: " + ioe.getMessage());
        } catch (CertificateException ce) {
            System.err.println("Error retrieving certificate: "
                    + ce.getMessage());
        } catch (CAClientException cace) {
            System.err.println("Error instantiating CA client: "
                    + cace.getMessage());
        }

        return null;
    }

    public static PrivateKey getPrivateKey(String entity) {
        try {
            KeyStore keyStore = CertUtil.readKeystoreFromResource(
                    entity + ".jks",
                    getUddiPassword().toCharArray());

            return CertUtil.getPrivateKeyFromKeyStore(entity.toLowerCase(),
                    getUddiPassword().toCharArray(), keyStore);
        } catch (KeyStoreException kse) {
            System.err.println("Error loading keystore: " + kse.getMessage());
        } catch (UnrecoverableKeyException uke) {
            System.err.println("Couldn't recover key: " + uke.getMessage());
        }

        return null;
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

    public Set<String> getTokens() {
    	return tokens;
    }

    public synchronized boolean addToken(String token) {
        return tokens.add(token);
    }
}
