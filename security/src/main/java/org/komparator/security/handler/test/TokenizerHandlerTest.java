package org.komparator.security.handler.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.komparator.security.SecurityManager;

/**
 * This SOAPHandler adds date and time to message header
 */
public class TokenizerHandlerTest implements SOAPHandler<SOAPMessageContext> {
	
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
			changeToken(smc);
		return true;
	}

	/** The handleFault method is invoked for fault message processing. */
	@Override
	public boolean handleFault(SOAPMessageContext smc) {
		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		if (outbound) 
			changeToken(smc);
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

	
	private void changeToken(SOAPMessageContext smc) {
			
		try {
			if(SecurityManager.getInstance().getTokens().size() > 0) {
				
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
						String errorMessage = "Can't perform test: Header not found";
						System.out.println(errorMessage);
						return;
					}
				}
				
				// get first header element
				Name name = se.createName(TOKEN_HEADER, NS_PREFIX, TOKEN_NS);
				
				@SuppressWarnings("rawtypes")
				Iterator it = sh.getChildElements(name);
				// check header element
				if (!it.hasNext()) {
					{
						String errorMessage = "Can't perform test: Header " + TOKEN_HEADER + " not found";
						System.out.println(errorMessage);
						return;
					}
				}
				SOAPElement element = (SOAPElement) it.next();
			
				// get header element value
				String token = (new ArrayList<String>(SecurityManager.getInstance().getTokens())).get(0);
				
				element.setTextContent(token);
			}
			
			
			
		} catch (SOAPException ie) {
			{
				String errorMessage = "Can't perform test: " + ie.getMessage();
				System.out.println(errorMessage);
				return;
			}
		}	
	}
}
