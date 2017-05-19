package org.komparator.security.handler;

import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;

import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.komparator.security.CryptoUtil;
import org.komparator.security.SecurityManager;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This SOAPHandler outputs the contents of inbound and outbound messages.
 */
public class EncryptionHandler implements SOAPHandler<SOAPMessageContext> {
	private static final String OPERATION_BUYCART = "buyCart";
	private static final String PARAM_CC_NUMBER = "creditCardNr";

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
		SOAPMessage msg = smc.getMessage();
		SOAPBody sb;

		try {
			sb = msg.getSOAPPart().getEnvelope().getBody();
		} catch (SOAPException soape) {
			String error = "SOAP error retrieving envelope's body"
					+ soape.getMessage();
			System.err.println(error);
			throw new RuntimeException(error);
		}

		QName operation = (QName) smc.get(MessageContext.WSDL_OPERATION);
		if (!operation.getLocalPart().equals(OPERATION_BUYCART)) {
			return true; // Not what we're looking for
		}

		NodeList nodes = sb.getFirstChild().getChildNodes();
		for (int i = 0; i < nodes.getLength(); ++i) {
			Node item = nodes.item(i);
			if (item.getNodeName().equals(PARAM_CC_NUMBER)) {
				byte[] result;

				System.out.printf("%n" + (outbound ? "En" : "De")
						+ "crypting credit card number%n%n");
				SecurityManager manager = SecurityManager.getInstance();
				if (outbound) {
					result = CryptoUtil.asymCipher(
							SecurityManager.getPublicKey(manager.getReceiver()),
							parseBase64Binary(item.getTextContent()));
				} else {
					result = CryptoUtil.asymDecipher(
							SecurityManager.getPrivateKey(manager.getSender()),
							parseBase64Binary(item.getTextContent()));
				}

				if (result == null) {
					throw new RuntimeException("Failed to "
							+ (outbound ? "en" : "de")
							+ "crypt credit card number");
				}

				item.setTextContent(printBase64Binary(result));
				try {
					msg.saveChanges();
				} catch (SOAPException soape) {
					String error = "SOAP message error: " + soape.getMessage();
					System.err.println(error);
					throw new RuntimeException(error);
				}

				return true;
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
