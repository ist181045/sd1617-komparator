package org.komparator.security.handler;

import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
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

	private static final String NS_PREFIX = "ns2";
	private static final String BUYCART_HEADER = "buyCart";
	private static final String CC_NS = "http://ws.mediator.komparator.org/";
	
	
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
		String sender = SecurityManager.getInstance().getSender();
		if(outbound && sender.equals("A58_Mediator"))
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

	/**
	 * Check the MESSAGE_OUTBOUND_PROPERTY in the context to see if this is an
	 * outgoing or incoming message. Write a brief message to the print stream
	 * and output the message. The writeTo() method can throw SOAPException or
	 * IOException
	 */
	private void encrypt(SOAPMessageContext smc) {
		
		try {
			
			SOAPMessage msg = smc.getMessage();
			SOAPPart sp = msg.getSOAPPart();
			SOAPEnvelope se = sp.getEnvelope();
			SOAPBody body = se.getBody();
			
			Name name = se.createName(BUYCART_HEADER, NS_PREFIX, CC_NS);
			
			
			@SuppressWarnings("rawtypes")
			Iterator it = body.getChildElements(name);
			// check header element
			if (!it.hasNext()) {
				{
					return;
				}
			}
			SOAPElement element = (SOAPElement) it.next();
			
			name = se.createName("creditCardNr", "", "");
			it = element.getChildElements(name);
			// check header element
			if (!it.hasNext()) {
				{
					return;
				}
			}
			element = (SOAPElement) it.next();
			
			System.out.println("Starting encrypt");
			
			PublicKey key = CertUtil.getPublicKeyFromCertificate(CertUtil.getX509CertificateFromResource("A58_Mediator.cer"));
			
			
			byte[] bytes = parseBase64Binary(element.getTextContent());
			
			element.setTextContent(printBase64Binary(CryptoUtil.asymCipher(key, bytes)));
			
			
		} catch (Exception e) {
			System.out.println("Couldn't encrypt credit card" + e.getMessage());
		}
		
	}
	
	private void decrypt(SOAPMessageContext smc) {
		
		try {
			
			SOAPMessage msg = smc.getMessage();
			SOAPPart sp = msg.getSOAPPart();
			SOAPEnvelope se = sp.getEnvelope();
			SOAPBody body = se.getBody();
			
			Name name = se.createName(BUYCART_HEADER, NS_PREFIX, CC_NS);
			
			
			@SuppressWarnings("rawtypes")
			Iterator it = body.getChildElements(name);
			// check header element
			if (!it.hasNext()) {
				{
					return;
				}
			}
			SOAPElement element = (SOAPElement) it.next();
			
			name = se.createName("creditCardNr", "", "");
			it = element.getChildElements(name);
			// check header element
			if (!it.hasNext()) {
				{
					return;
				}
			}
			element = (SOAPElement) it.next();
			
			System.out.println("Starting decrypt");

			
			PrivateKey key = CertUtil.getPrivateKeyFromKeyStoreResource("A58_Mediator.jks", SecurityManager.getInstance().getPassword().toCharArray(), "a58_mediator", SecurityManager.getInstance().getPassword().toCharArray());			
			
			byte[] bytes = parseBase64Binary(element.getTextContent());
			
			element.setTextContent(printBase64Binary(CryptoUtil.asymDecipher(key, bytes)));
			
			
		} catch (Exception e) {
			System.out.println("Couldn't decrypt credit card" + e.getMessage());
		}
	}

}
