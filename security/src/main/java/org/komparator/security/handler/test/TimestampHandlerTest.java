package org.komparator.security.handler.test;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.komparator.security.SecurityManager;

/**
 * This SOAPHandler adds date and time to message header
 */
public class TimestampHandlerTest implements SOAPHandler<SOAPMessageContext> {
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
		if (outbound) {
			try {
				int timeout = SecurityManager.getMsgTimeout();
				System.out.printf("%n[TIMESTAMP ATTACK] Sleeping %d seconds..",
						timeout);
				TimeUnit.SECONDS.sleep(timeout);
				System.out.printf("OK%n%n");
			} catch (InterruptedException ie) {
				System.err.printf("KO%n%n");
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
