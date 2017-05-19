package org.komparator.security.handler;

import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
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

import pt.ulisboa.tecnico.sdis.cert.CertUtil;
import pt.ulisboa.tecnico.sdis.ws.cli.CAClient;
import pt.ulisboa.tecnico.sdis.ws.cli.CAClientException;

/**
 * This SOAPHandler signs or verifies the signature of outbound and inbound messages respectively.
 */
public class SignatureHandler implements SOAPHandler<SOAPMessageContext> {
	private static final String DIGEST_ALGO = "SHA256WithRSA";
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

		String entLocalName = "Sender";
		String sigLocalName = "Signature";
		String prefix = service.getLocalPart().substring(0, 3).toLowerCase();
		String uri = service.getNamespaceURI();

		SOAPMessage msg = smc.getMessage();
		SOAPPart sp = msg.getSOAPPart();
		SOAPEnvelope se;
		SOAPHeader sh;
		try {
			se = sp.getEnvelope();
			sh = se.getHeader();

			if (sh == null) {
				if (outbound) {
					sh = se.addHeader();
				} else {
					String error = "Couldn't verify signature, header missing";
					System.err.printf("%n%s%n%n", error);
					throw new RuntimeException(error);
				}
			}
		} catch (SOAPException soape) {
			String error = "SOAP error getting header";
			System.err.printf("%n%s: %s%n%n", error, soape.getMessage());
			throw new RuntimeException(error, soape);
		}

		if (outbound) {
			/* sign the message */
			String sender = SecurityManager.getInstance().getSender();

			System.out.printf("%nAdding entity to message's header.. ");
			System.out.flush();
			addHeaderElement(smc, sender, entLocalName, prefix, uri);
			System.out.printf("OK: %s%n%n", sender);

			PrivateKey key = SecurityManager.getPrivateKey(sender);
			System.out.printf("%nSigning SOAP message.. ");
			if (key == null) {
				String error = "Couldn't get " + sender + "'s private key";
				System.err.printf("KO: %s%n%n", error);
				throw new RuntimeException(error);
			}

			byte[] msgBytes = getMessageBytes(msg);
			byte[] signature =
					CertUtil.makeDigitalSignature(DIGEST_ALGO, key, msgBytes);
			if (signature == null) {
				String error = "Couldn't make digital signature";
				System.err.printf("KO: %s%n%n", error);
				throw new RuntimeException(error);
			}

			String sig = printBase64Binary(signature);
			addHeaderElement(smc, sig, sigLocalName, prefix, uri);
			System.out.printf("OK: %s...%n%n", sig.substring(0, 16));
		} else {
			/* verify message's signature */
			NodeList nodes = sh.getElementsByTagNameNS(uri, entLocalName);
			String ent = nodes.item(0).getTextContent();

			CAClient caClient;
			System.out.printf("%nVerifying message's signature.. ");
			try {
				caClient = new CAClient(SecurityManager.getCaUrl());
			} catch (CAClientException cace) {
				String error = "Couldn't communicate with CA";
				System.err.printf("KO: %s: %s%n%n", error, cace.getMessage());
				throw new RuntimeException(error, cace);
			}

			String certString = caClient.getCertificate(ent);
			Certificate cert;
			try {
				cert = CertUtil.getX509CertificateFromPEMString(certString);
				Certificate caCert =
						CertUtil.getX509CertificateFromResource("ca.cer");
				if (!CertUtil.verifySignedCertificate(cert, caCert)) {
					String error = "Sender certificate invalid";
					System.err.println(error);
					throw new RuntimeException(error);
				}
			} catch (CertificateException | IOException e) {
				String error = "Failed to get certificate";
				System.err.printf("KO: %s: %s%n%n", error, e);
				throw new RuntimeException(error, e);
			}

			nodes = sh.getElementsByTagNameNS(uri, sigLocalName);
			String sig = nodes.item(0).getTextContent();
			sh.removeChild(nodes.item(0));
			try {
				msg.saveChanges();
			} catch (SOAPException soape) {
				String error = "Couldn't save message's changes";
				System.err.printf("KO: %s: %s%n%n", error, soape.getMessage());
				throw new RuntimeException(error, soape);
			}

			byte[] sigBytes = parseBase64Binary(sig);
			byte[] msgBytes = getMessageBytes(msg);

			if (!CertUtil.verifyDigitalSignature(DIGEST_ALGO, cert,
					msgBytes, sigBytes)) {
				String error = "Couldn't validate message's signature";
				System.err.println(error);
				throw new RuntimeException(error);
			}
			System.out.printf("OK%n%n");
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

	private void addHeaderElement(SOAPMessageContext smc, String content,
			String localPart, String prefix, String namespaceUri) {
		try {
			SOAPMessage msg = smc.getMessage();
			SOAPEnvelope se = msg.getSOAPPart().getEnvelope();
			SOAPHeader sh = se.getHeader();

			Name name = se.createName(localPart, prefix, namespaceUri);
			SOAPHeaderElement element = sh.addHeaderElement(name);
			element.addTextNode(content);
			msg.saveChanges();
		} catch (SOAPException soape) {
			String error = "Couldn't add '" + localPart + "' to message header";
			System.err.printf("KO: %s: %s%n%n", error, soape.getMessage());
			throw new RuntimeException(error, soape);
		}
	}

	private byte[] getMessageBytes(SOAPMessage msg) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		try {
			msg.writeTo(bos);
		} catch (SOAPException | IOException e) {
			String error = "Couldn't write message to stream";
			System.err.printf("KO: %s: %s%n%n", error, e.getMessage());
			throw new RuntimeException(error, e);
		}

		return parseBase64Binary(new String(bos.toByteArray()));
	}

}
