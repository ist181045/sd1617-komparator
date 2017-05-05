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

/**
 * This SOAPHandler adds date and time to message header
 */
public class TimestampHandler implements SOAPHandler<SOAPMessageContext> {

	
	

	private static final String NS_PREFIX = "fre";
	private static final String TIMESTAMP_HEADER = "Timestamp";
	private static final String TIMESTAMP_NS = "urn:fresh";
	
	
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
			addTimestamp(smc);
		else
			checkTimestamp(smc);
		return true;
	}

	/** The handleFault method is invoked for fault message processing. */
	@Override
	public boolean handleFault(SOAPMessageContext smc) {
		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		if (outbound) 
			addTimestamp(smc);
		else
			checkTimestamp(smc);
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
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

	
	private void addTimestamp(SOAPMessageContext smc) {
			
		try {
			
			// get SOAP envelope
			SOAPMessage msg = smc.getMessage();
			SOAPPart sp = msg.getSOAPPart();
			SOAPEnvelope se = sp.getEnvelope();

			// add header if it doesn't exist
			SOAPHeader sh = se.getHeader();
			if (sh == null)
				sh = se.addHeader();
					
			
			Name name = se.createName(TIMESTAMP_HEADER, NS_PREFIX, TIMESTAMP_NS);
			SOAPHeaderElement element = sh.addHeaderElement(name);
			
			// add message time
			String now = dateFormatter.format(new Date());
			element.addTextNode(now);
			
			System.out.printf("%nAdded timestamp to outbound message %s%n%n",now);
			
			
		} catch (SOAPException se) {
			{
				String errorMessage = "Couldn't add date to message: " + se.getMessage();
				System.out.println(errorMessage);
				throw new RuntimeException(errorMessage);
			}
		}
		
	}
	
	private void checkTimestamp(SOAPMessageContext smc) {
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
			Name name = se.createName(TIMESTAMP_HEADER, NS_PREFIX, TIMESTAMP_NS);
			
			@SuppressWarnings("rawtypes")
			Iterator it = sh.getChildElements(name);
			// check header element
			if (!it.hasNext()) {
				{
					String errorMessage = "Header " + TIMESTAMP_HEADER + " not found";
					System.out.println(errorMessage);
					throw new RuntimeException(errorMessage);
				}
			}
			SOAPElement element = (SOAPElement) it.next();
		
			// get header element value
			String headerValue = element.getValue();
			Date messageTimestamp = dateFormatter.parse(headerValue);

			long seconds = (new Date().getTime() - messageTimestamp.getTime())/1000;
			
			if (seconds < 0 || seconds > SecurityManager.getMaxTimeout() ){
				{
					String errorMessage = "Message time invalid";
					System.out.println(errorMessage);
					throw new RuntimeException(errorMessage);
				}
			}
			
			System.out.printf("%nVerified timestamp %n%n");
		} catch (SOAPException se) {
			{
				String errorMessage = "Couldn't get date from message: " + se.getMessage();
				System.out.println(errorMessage);
				throw new RuntimeException(errorMessage);
			}
		} catch (ParseException pe) {
			String errorMessage = "Couldn't parse date: " + pe.getMessage();
			System.out.println(errorMessage);
			throw new RuntimeException(errorMessage);
		}
	}

}
