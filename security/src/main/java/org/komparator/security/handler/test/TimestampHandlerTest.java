package org.komparator.security.handler.test;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.komparator.security.handler.TimestampHandler;

/**
 * This SOAPHandler adds date and time to message header
 */
public class TimestampHandlerTest implements SOAPHandler<SOAPMessageContext> {
	
	private static final int TIMEOUT = TimestampHandler.TIMEOUT + 1;
	
	
	
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
			delayMessage(smc);
		return true;
	}

	/** The handleFault method is invoked for fault message processing. */
	@Override
	public boolean handleFault(SOAPMessageContext smc) {
		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		if (outbound) 
			delayMessage(smc);
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

	
	private void delayMessage(SOAPMessageContext smc) {
			
		try {
			
			System.out.println("Sleeping :" + TIMEOUT + " seconds");
			
			TimeUnit.SECONDS.sleep(TIMEOUT);
			
			System.out.println("Sleep time is over");
			
			
		} catch (InterruptedException ie) {
			{
				String errorMessage = "Couldn't sleep: " + ie.getMessage();
				System.out.println(errorMessage);
				throw new RuntimeException(errorMessage);
			}
		}	
	}
}
