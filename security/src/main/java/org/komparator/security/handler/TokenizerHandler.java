package org.komparator.security.handler;

import static javax.xml.bind.DatatypeConverter.printBase64Binary;

import java.security.SecureRandom;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
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

/**
 * This SOAPHandler adds date and time to message header
 */
public class TokenizerHandler implements SOAPHandler<SOAPMessageContext> {
	private static final String NS_PREFIX = "tok";
	private static final String TOKEN_HEADER = "Token";
	private static final String TOKEN_NS = "urn:token";

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
		if (outbound) 
			addToken(smc);
		else
			checkToken(smc);
		return true;
	}

	/** The handleFault method is invoked for fault message processing. */
	@Override
	public boolean handleFault(SOAPMessageContext smc) {
		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		if (outbound) 
			addToken(smc);
		else
			checkToken(smc);
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
	
	private void addToken(SOAPMessageContext smc) {
			
		try {
			// get SOAP envelope
			SOAPMessage msg = smc.getMessage();
			SOAPPart sp = msg.getSOAPPart();
			SOAPEnvelope se = sp.getEnvelope();

			// add header if it doesn't exist
			SOAPHeader sh = se.getHeader();
			if (sh == null)
				sh = se.addHeader();

			Name name = se.createName(TOKEN_HEADER, NS_PREFIX, TOKEN_NS);
			SOAPHeaderElement element = sh.addHeaderElement(name);
			
			// create token
			String token = null;
			do {
				SecureRandom random = new SecureRandom();
			    byte bytes[] = new byte[32];
			    random.nextBytes(bytes);
			    token = printBase64Binary(bytes);
			}
			while (SecurityManager.getInstance().getTokens().contains(token));
			
			element.addTextNode(token);
			
			System.out.printf("%nAdded token to outbound message %s%n%n",token);
			
			
		} catch (SOAPException se) {
			{
				String errorMessage = "Couldn't add token to message: " + se.getMessage();
				System.out.println(errorMessage);
				throw new RuntimeException(errorMessage);
			}
		}
		
	}
	
	private void checkToken(SOAPMessageContext smc) {
		try {
			
			SOAPMessage msg = smc.getMessage();
			SOAPPart sp = msg.getSOAPPart();
			SOAPEnvelope se = sp.getEnvelope();
			SOAPHeader sh = se.getHeader();
			
			if(msg.getSOAPBody().getFault() != null) {
				return;
			}

			// check header
			if (sh == null) {
				{
					String errorMessage = "Header not found";
					System.out.println(errorMessage);
					throw new RuntimeException(errorMessage);
				}
			}
			
			// get first header element
			Name name = se.createName(TOKEN_HEADER, NS_PREFIX, TOKEN_NS);
			
			@SuppressWarnings("rawtypes")
			Iterator it = sh.getChildElements(name);
			// check header element
			if (!it.hasNext()) {
				{
					String errorMessage = "Header " + TOKEN_HEADER + " not found";
					System.out.println(errorMessage);
					throw new RuntimeException(errorMessage);
				}
			}
			SOAPElement element = (SOAPElement) it.next();
		
			// get header element value
			String token = element.getValue();
			
			if (SecurityManager.getInstance().getTokens().contains(token)){
				{
					String errorMessage = "Token invalid";
					System.out.println(errorMessage);
					throw new RuntimeException(errorMessage);
				}
			}
			
			SecurityManager.getInstance().getTokens().add(token);
			
			System.out.printf("%nVerified token %n%n");
			
		} catch (SOAPException se) {
			{
				String errorMessage = "Couldn't get token from message: " + se.getMessage();
				System.out.println(errorMessage);
				throw new RuntimeException(errorMessage);
			}
		}
	}

}
