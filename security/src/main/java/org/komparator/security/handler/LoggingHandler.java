package org.komparator.security.handler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

/**
 * This SOAPHandler outputs the contents of inbound and outbound messages.
 */
public class LoggingHandler implements SOAPHandler<SOAPMessageContext> {

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
		logToSystemOut(smc);
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
	private SimpleDateFormat dateFormatter =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

	/**
	 * Check the MESSAGE_OUTBOUND_PROPERTY in the context to see if this is an
	 * outgoing or incoming message. Write a brief message to the print stream
	 * and output the message. The newTransformer() method can throw a
	 * TransformerConfigurationExcpetion while the transform() method can throw
	 * a TransformerException (both caught and ignored).
	 */
	private void logToSystemOut(SOAPMessageContext smc) {
		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		
		if(((String)smc.get(MessageContext.WSDL_OPERATION)).equals("imAlive")) {
			if(outbound)
				System.out.printf("%nSent imAlive%n%n");
			else
				System.out.printf("%nReceived imAlive%n%n");
			return;
		}

		// print current timestamp
		System.out.print("[");
		System.out.print(dateFormatter.format(new Date()));
		System.out.print("] ");

		System.out.print("intercepted ");
		if (outbound)
			System.out.print("OUTbound");
		else
			System.out.print(" INbound");
		System.out.println(" SOAP message:");

		SOAPMessage message = smc.getMessage();
		try {
			Source source = new DOMSource(message.getSOAPPart());

			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer t = tf.newTransformer();

			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.setOutputProperty(OutputKeys.STANDALONE, "yes");
			t.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "2");

			StreamResult result = new StreamResult(System.out);
			t.transform(source, result);
		} catch (TransformerException te) {
			System.err.print("Ignoring " + te.getClass().getCanonicalName()
					+ " in handler: ");
			System.err.println(te);
		}
	}

}
