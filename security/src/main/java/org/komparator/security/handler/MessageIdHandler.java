package org.komparator.security.handler;

import java.util.Set;

import org.komparator.security.*;
import org.komparator.security.SecurityManager;
import org.w3c.dom.NodeList;

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

/**
 * This SOAPHandler outputs the contents of inbound and outbound messages.
 */
public class MessageIdHandler implements SOAPHandler<SOAPMessageContext> {
	public static final String MESSAGE_ID_PROPERTY = "org.komparator.mediator.ws.message.id";
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
		logToSystemOut(smc);
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
	 * and output the message. The newTransformer() method can throw a
	 * TransformerConfigurationExcpetion while the transform() method can throw
	 * a TransformerException (both caught and ignored).
	 */
	private void logToSystemOut(SOAPMessageContext smc) {
		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		QName service = (QName)smc.get(MessageContext.WSDL_SERVICE);

		String localName = "MessageId";
		String prefix = service.getLocalPart().substring(0, 3).toLowerCase();
		String uri = service.getNamespaceURI();

		SOAPMessage msg = smc.getMessage();
		SOAPPart sp = msg.getSOAPPart();
		try {
			SOAPEnvelope se = sp.getEnvelope();
			SOAPHeader sh = se.getHeader();

			String sender = SecurityManager.getInstance().getSender();
			if (sender.equals("MediatorClient")) {
				String messageId = (String) smc.get(MESSAGE_ID_PROPERTY);
				if (messageId == null) return;

				Name name = se.createName(localName, prefix, uri);
				SOAPHeaderElement element = sh.addHeaderElement(name);
				element.addTextNode(messageId);
				msg.saveChanges();
			} else {
				NodeList nodes = sh.getElementsByTagNameNS(uri, localName);
				if (nodes.getLength() == 0) return;

				String messageId = nodes.item(0).getTextContent();
				// TODO: Handle message id and responses
			}
		} catch (SOAPException e) {
			System.err.println("bork");
		}

	}

}
