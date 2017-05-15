package org.komparator.security.handler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This SOAPHandler adds date and time to message header
 */
public class TimestampHandler implements SOAPHandler<SOAPMessageContext> {
	//
	// Handler interface implementation
	//

    /** Date formatter used for outputting timestamps in ISO 8601 format */
    private SimpleDateFormat dateFormatter =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

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

        String localName = "Timestamp";
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
                    String error = "Couldn't verify timestamp, header missing";
                    System.err.println(error);
                    throw new RuntimeException(error);
                }
            }

            if (outbound) {
                Name name = se.createName(localName, prefix, uri);
                SOAPHeaderElement element = sh.addHeaderElement(name);
                String now = dateFormatter.format(new Date());

                // add timestamp to header
                System.out.printf("%nAdding timestamp to message.. ");
                element.addTextNode(now);
                msg.saveChanges();

                System.out.printf("OK: %s%n%n", now);
            } else {
                NodeList nodes = sh.getElementsByTagNameNS(uri, localName);
                String msgTimestamp = nodes.item(0).getTextContent();

                try {
                    long diff = new Date().getTime()
                            - dateFormatter.parse(msgTimestamp).getTime();

                    System.out.printf("%nChecking timestamp in message.. ");
                    // check time difference (negative or > 3 secs == bad)
                    if (diff < 0 || (diff / 1000F)
                            > SecurityManager.getMsgTimeout()) {
                        String error =
                                "Invalid timestamp in message";
                        System.err.printf("KO: %s%n%n", error);
                        throw new RuntimeException(error);
                    }
                } catch (ParseException pe) {
                    String error = "Couldn't parse date: " + msgTimestamp;
                    System.err.printf("KO: %s: %s%n%n", error, pe.getMessage());
                    throw new RuntimeException(error, pe);
                }

                System.out.printf("OK%n%n");
            }

        } catch (SOAPException soape) {
            String error = "Couldn't "
                    + (outbound ? "add" : "verify message's")
                    + " timestamp "
                    + (outbound ? "to message" : "");
            System.err.printf("%n%s: %s%n%n", error, soape.getMessage());
            throw new RuntimeException(error, soape);
        }

		return true;
	}

	/** The handleFault method is invoked for fault message processing. */
	@Override
	public boolean handleFault(SOAPMessageContext smc) {
		/*if (outbound)
			addTimestamp(smc);
		else
			checkTimestamp(smc);*/
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
