package org.komparator.security.handler;

import static javax.xml.bind.DatatypeConverter.printBase64Binary;

import java.security.SecureRandom;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This SOAPHandler adds date and time to message header
 */
public class TokenizerHandler implements SOAPHandler<SOAPMessageContext> {
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
		QName service = (QName) smc.get(MessageContext.WSDL_SERVICE);

		String localName = "SecurityToken";
		String prefix = service.getLocalPart().substring(0, 3).toLowerCase();
		String uri = service.getNamespaceURI();

		try {
			SOAPMessage msg = smc.getMessage();
			SOAPPart sp = msg.getSOAPPart();
			SOAPEnvelope se = sp.getEnvelope();
			SOAPHeader sh = se.getHeader();

			// check if header exists
			if (sh == null) {
				if (outbound) {
					// add one if outbound
					sh = se.addHeader();
				} else {
					// throw exception if inbound
					String error = "Couldn't verify token, header missing";
					System.err.println(error);
					throw new RuntimeException(error);
				}
			}

			if (outbound) {
				String token;

				System.out.printf("%nAdding token to message.. ");
				do {
					byte[] bytes = new byte[32];
					SecureRandom random = new SecureRandom();

					random.nextBytes(bytes);
					token = printBase64Binary(bytes);
					// generate a new token if this one exists
				} while (!SecurityManager.getInstance().addToken(token));

				Name name = se.createName(localName, prefix, uri);
				SOAPHeaderElement element = sh.addHeaderElement(name);
				element.addTextNode(token);
				msg.saveChanges();

				System.out.printf("OK: %s%n%n", token);
			} else {
				System.out.printf("%nChecking token in message.. ");

				NodeList nodes = sh.getElementsByTagNameNS(uri, localName);
				if (nodes.getLength() == 0) {
					String error = "Couldn't find token in header";
					System.out.printf("KO: %s%n%n", error);
					throw new RuntimeException(error);
				}

				Node item = nodes.item(0);
				if (!SecurityManager.getInstance()
						.addToken(item.getTextContent())) {
					// throw exception if it already exists
					String error = "Invalid token (already received)";
					System.out.printf("KO: %s%n%n", error);
					throw new RuntimeException(error);
				}

				System.out.printf("OK%n%n");
			}
		} catch (SOAPException soape) {
			String error = "Couldn't "
					+ (outbound ? "add" : "verify message's")
					+ " token "
					+ (outbound ? " to message" : "");
			System.err.printf("%n%s: %s%n%n", error, soape.getMessage());
			throw new RuntimeException(error, soape);
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
