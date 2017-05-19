package org.komparator.security.handler.test;

import java.util.Set;

import javax.xml.namespace.QName;
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
import org.w3c.dom.NodeList;

/**
 * This SOAPHandler adds date and time to message header
 */
public class TokenizerHandlerTest implements SOAPHandler<SOAPMessageContext> {
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

		Set<String> tokens = SecurityManager.getInstance().getTokens();
		if (outbound && tokens.size() > 1)  {
			QName service = (QName) smc.get(MessageContext.WSDL_SERVICE);

			String localName = "SecurityToken";
			String uri = service.getNamespaceURI();

			System.err.print("[TOKEN ATTACK] Starting.. ");
			try {
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				SOAPHeader sh = se.getHeader();

				// check header
				if (sh == null) {
					System.err.printf("KO: Header not found, "
							+ "exiting..%n%n");
					return true;
				}

				// get first header element
				NodeList nodes = sh.getElementsByTagNameNS(uri, localName);
				if (nodes.getLength() == 0) {
					System.err.printf("KO: Couldn't find token "
							+ "in header, exiting..%n%n");
					return true;
				}

				// get header element and inject value
				SOAPElement element = (SOAPElement) nodes.item(0);
				String token = tokens.stream().findFirst().get();
				element.setTextContent(token);
				msg.saveChanges();

				System.err.printf("OK: Older token injected!%n%n");
			} catch (SOAPException soape) {
				System.err.printf("KO: Soap error: %s%n%n", soape.getMessage());
			}
		}

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
}
