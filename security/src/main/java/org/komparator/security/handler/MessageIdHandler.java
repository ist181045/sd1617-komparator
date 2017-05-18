package org.komparator.security.handler;

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
import org.w3c.dom.NodeList;

/**
 * This SOAPHandler outputs the contents of inbound and outbound messages.
 */
public class MessageIdHandler implements SOAPHandler<SOAPMessageContext> {
	private static final String MESSAGE_ID_PROPERTY =
            "org.komparator.mediator.ws.message.id";
	
	private static final String OPERATION_BUYCART = "buyCart";
	private static final String OPERATION_ADDTOCART = "addToCart";
	
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
        QName service = (QName)smc.get(MessageContext.WSDL_SERVICE);

        String localName = "MessageId";
        String prefix = service.getLocalPart().substring(0, 3).toLowerCase();
        String uri = service.getNamespaceURI();

        SOAPMessage msg = smc.getMessage();
        SOAPPart sp = msg.getSOAPPart();
        SOAPEnvelope se;
        SOAPHeader sh;
        try {
            se = sp.getEnvelope();
            sh = se.getHeader();
        } catch (SOAPException soape) {
            String error = "SOAP error retrieving envelope/header: "
                    + soape.getMessage();
            System.err.println(error);
            throw new RuntimeException(error);
        }

        QName operation = (QName)smc.get(MessageContext.WSDL_OPERATION);
        String opName = operation.getLocalPart();
        if (opName.equals(OPERATION_ADDTOCART)
                || opName.equals(OPERATION_BUYCART)) {
            if (outbound) {
                String sender = SecurityManager.getInstance().getSender();
                if (sender.equals("MediatorClient")) {
                    String messageId = (String)smc.get(MESSAGE_ID_PROPERTY);
                    if (messageId == null) {
                        String error =
                                "Couldn't find message id in message context";
                        System.err.println(error);
                        throw new RuntimeException(error);
                    }

                    try {
                        Name name = se.createName(localName, prefix, uri);
                        SOAPHeaderElement element = sh.addHeaderElement(name);
                        element.addTextNode(messageId);
                        msg.saveChanges();
                    } catch (SOAPException e) {
                        String error = "SOAP error adding message id to header: "
                                + e.getMessage();
                        System.err.println(error);
                        throw new RuntimeException(error);
                    }
                }
            } else {
                NodeList nodes = sh.getElementsByTagNameNS(uri, localName);
                if (nodes.getLength() > 0) {
                    String messageId = nodes.item(0).getTextContent();
                    smc.put(MESSAGE_ID_PROPERTY, messageId);
                } else {
                    String error = "Couldn't add message id to message context";
                    System.err.println(error);
                    throw new RuntimeException(error);
                }
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
