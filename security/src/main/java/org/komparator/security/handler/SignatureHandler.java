package org.komparator.security.handler;

import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.komparator.security.SecurityManager;

import pt.ulisboa.tecnico.sdis.cert.CertUtil;
import pt.ulisboa.tecnico.sdis.ws.cli.CAClient;
import pt.ulisboa.tecnico.sdis.ws.cli.CAClientException;

/**
 * This SOAPHandler signs or verifies the signature of outbound and inbound messages respectively.
 */
public class SignatureHandler implements SOAPHandler<SOAPMessageContext> {

	//
	// Handler interface implementation
	//
	
	private static final String KEYSTORE_PASSWORD = "M6cggAUT";
	
	private static final String CA_URL = "http://sec.sd.rnl.tecnico.ulisboa.pt:8081/ca";
	
	private static final String ENTITY_NAME = "entity_name";
	private static final String ENTITY_PREFIX = "ent";
	private static final String ENTITY_NAMESPACE = "ent:entity";
	
	public static final String SIGNATURE_NAME = "signature";
	public static final String SIGNATURE_PREFIX = "sig";
	public static final String SIGNATURE_NAMESPACE = "sig:signature";

	/**
	 * Gets the header blocks that can be processed by this Handler instance. If
	 * null, processes all.
	 */
	@Override
	public Set<QName> getHeaders() {
		return null;
	}

	/**
	 * The handleMessage method is invoked for normal processing of inbound and
	 * outbound messages.
	 */
	@Override
	public boolean handleMessage(SOAPMessageContext smc) {
		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		if(outbound)
			addSignature(smc);
		else
			verifySignature(smc);
		
		return true;
	}

	/** The handleFault method is invoked for fault message processing. */
	@Override
	public boolean handleFault(SOAPMessageContext smc) {
		return true;
	}

	/**
	 * Called at the conclusion of a message exchange pattern just prior to the
	 * JAX-WS runtime dispatching a message, fault or exception.
	 */
	@Override
	public void close(MessageContext messageContext) {
		// nothing to clean up
	}
	
	private void addSignature(SOAPMessageContext smc) {
		
		
		try {
			String entity = SecurityManager.getInstance().getSender();
			
			if(entity == null)
				throw new RuntimeException("Entity is null");
			
			SOAPMessage message = smc.getMessage();
			SOAPPart part = message.getSOAPPart();
			SOAPEnvelope envelope = part.getEnvelope();
			SOAPHeader header = envelope.getHeader();
			
			if(header == null)
				header = envelope.addHeader();
			
			Name name = envelope.createName(ENTITY_NAME, ENTITY_PREFIX, ENTITY_NAMESPACE);
			
			SOAPHeaderElement element = header.addHeaderElement(name);
			
			element.addTextNode(entity);
			
			System.out.printf("%nAdded entity name to SOAP header: " + entity + "%n%n");
			
			KeyStore keystore = CertUtil.readKeystoreFromResource(entity + ".jks", KEYSTORE_PASSWORD.toCharArray());
			
			PrivateKey key = CertUtil.getPrivateKeyFromKeyStore(entity.toLowerCase(), KEYSTORE_PASSWORD.toCharArray(), keystore);

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			message.writeTo(out);
			byte[] bytes = parseBase64Binary(new String(out.toByteArray()));
			
			byte[] digest = CertUtil.makeDigitalSignature("SHA256WithRSA", key, bytes);
			SOAPHeaderElement signature = header.addHeaderElement(envelope.createName(SIGNATURE_NAME, SIGNATURE_PREFIX, SIGNATURE_NAMESPACE));;
			
			signature.addTextNode(printBase64Binary(digest));
			
			System.out.printf("%nSigned SOAP header%n%n");
			
		} catch (SOAPException se) {
			System.out.println("Signature Handler caught a SOAPException: " + se.getMessage());
		} catch(IOException ioe) {
			System.out.println("Signature Handler caught an IOException: " + ioe.getMessage());
		} catch (KeyStoreException kse) {
			System.out.println("Signature Handler caught a KeyStoreException: " + kse.getMessage());
		} catch (UnrecoverableKeyException uke) {
			System.out.println("Signature Handler caught an UnrecoverableKeyException: " + uke.getMessage());
		}
		
	}
	
private void verifySignature(SOAPMessageContext smc) {
		
		
		try {
			SOAPMessage message = smc.getMessage();
			SOAPPart part = message.getSOAPPart();
			SOAPEnvelope envelope = part.getEnvelope();
			SOAPHeader header = envelope.getHeader();
			
			if(message.getSOAPBody().getFault() != null) {
				return;
			}
			
			Name name = envelope.createName(ENTITY_NAME, ENTITY_PREFIX, ENTITY_NAMESPACE);
			Iterator it = header.getChildElements(name);
			
			if(!it.hasNext()) {
				String errorMessage = "Couldn't get Entity field from header";
				System.out.println(errorMessage);
				throw new RuntimeException(errorMessage);
			}
			
			SOAPElement element = (SOAPElement) it.next();
			String entity = element.getValue();
			
			CAClient ca = new CAClient(CA_URL);
			
			String cString = ca.getCertificate(entity);
			
			System.out.printf("%nReceived Certificate from CA%n%n");
			
			byte[] bytes = cString.getBytes(StandardCharsets.UTF_8);
			InputStream in = new ByteArrayInputStream(bytes);
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			Certificate cert = certFactory.generateCertificate(in);
			
			
			if(!CertUtil.verifySignedCertificate(cert, CertUtil.getX509CertificateFromResource("ca.cer"))) {
				String errorMessage = "Certificate from CA not valid";
				System.out.println(errorMessage);
				throw new RuntimeException(errorMessage);
			}
			
			name = envelope.createName(SIGNATURE_NAME, SIGNATURE_PREFIX, SIGNATURE_NAMESPACE);
			
			it = header.getChildElements(name);
			
			if(!it.hasNext()) {
				String errorMessage = "Couldn't get Signature field from header";
				System.out.println(errorMessage);
				throw new RuntimeException(errorMessage);
			}
			
			element = (SOAPElement) header.removeChild((Node)it.next());
			String signature = element.getValue();
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			message.writeTo(out);
			bytes = parseBase64Binary(new String(out.toByteArray()));
			
			if(!CertUtil.verifyDigitalSignature("SHA256WithRSA", cert, bytes, parseBase64Binary(signature))) {
				String errorMessage = "Signature was not correctly verified";
				System.out.println(errorMessage);
				throw new RuntimeException(errorMessage);
			}
			
			System.out.printf("%nVerified valid Signature%n%n");
			
		} catch (SOAPException se) {
			System.out.println("Signature Handler caught a SOAPException: " + se.getMessage());
		} catch(CertificateException ce) {
			System.out.println("Signature Handler caught a CertificateException: " + ce.getMessage());
		} catch(IOException ioe) {
			System.out.println("Signature Handler caught an IOException: " + ioe.getMessage());
		} catch(CAClientException cae) {
			System.out.println("Signature Handler caught a CAClientException: " + cae.getMessage());
		}
		
	}

}
