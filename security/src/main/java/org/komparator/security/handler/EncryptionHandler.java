package org.komparator.security.handler;

import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.komparator.security.CryptoUtil;
import org.komparator.security.SecurityManager;

import pt.ulisboa.tecnico.sdis.cert.CertUtil;

/**
 * This SOAPHandler outputs the contents of inbound and outbound messages.
 */
public class EncryptionHandler implements SOAPHandler<SOAPMessageContext> {

	//
	// Handler interface implementation
	//

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
			encrypt(smc);
		else
			decrypt(smc);
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

	/** Date formatter used for outputting timestamps in ISO 8601 format */
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

	/**
	 * Check the MESSAGE_OUTBOUND_PROPERTY in the context to see if this is an
	 * outgoing or incoming message. Write a brief message to the print stream
	 * and output the message. The writeTo() method can throw SOAPException or
	 * IOException
	 */
	private void encrypt(SOAPMessageContext smc) {
		
		try {
			
			SOAPMessage msg = smc.getMessage();
			SOAPEnvelope env = msg.getSOAPPart().getEnvelope();
			
			String destination = SecurityManager.getInstance().getDestination();
			
			if(destination == null) {
				throw new RuntimeException("Destination entity is null");
			}
			
			PublicKey key = CertUtil.getPublicKeyFromCertificate(CertUtil.getX509CertificateFromResource(destination + ".cer"));
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			msg.writeTo(out);
			byte[] bytes = parseBase64Binary(new String(out.toByteArray()));
			
			env.setTextContent(printBase64Binary(CryptoUtil.asymCipher(key, bytes)));
			
			
		} catch (SOAPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void decrypt(SOAPMessageContext smc) {
		
		try {
			
			SOAPMessage msg = smc.getMessage();
			SOAPEnvelope env = msg.getSOAPPart().getEnvelope();
			
			String entity = SecurityManager.getInstance().getSender();
			
			if(entity == null) {
				throw new RuntimeException("Entity is null");
			}
			
			PrivateKey key = CertUtil.getPrivateKeyFromKeyStoreResource(entity + ".jks", SecurityManager.getInstance().getPassword().toCharArray(), entity.toLowerCase(), SecurityManager.getInstance().getPassword().toCharArray());
			
			/*ByteArrayOutputStream out = new ByteArrayOutputStream();
			msg.writeTo(out);
			byte[] bytes = parseBase64Binary(new String(out.toByteArray()));*/
			
			byte[] bytes = parseBase64Binary(env.getTextContent());
			
			CryptoUtil.asymCipher(key, bytes);
			
			
		} catch (SOAPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
