package org.komparator.mediator.ws;

import java.util.Timer;

public class MediatorApp {

	public static void main(String[] args) throws Exception {
		// Check arguments
		if (args.length == 0 || args.length == 2) {
			System.err.println("Argument(s) missing!");
			System.err.println("Usage: java " + MediatorApp.class.getName() + " wsURL OR uddiURL wsName wsURL");
			return;
		}
		String uddiURL = null;
		String wsName = null;
		String wsURL = null;
		int wsI = 0;

		// Create server implementation object, according to options
		MediatorEndpointManager endpoint = null;
		if (args.length == 1) {
			wsURL = args[0];
			endpoint = new MediatorEndpointManager(wsURL);
		} else if (args.length >= 3) {
			uddiURL = args[0];
			wsName = args[1];
			wsURL = args[2];
			if(args[3] != null)
				wsI = Integer.parseInt(args[3]);
			else 
				wsI = Integer.parseInt(String.valueOf(wsURL.charAt(wsURL.indexOf(":", wsURL.indexOf(":") + 1) + 4)));
			
			endpoint = new MediatorEndpointManager(uddiURL, wsName, wsURL, wsI);
			
			endpoint.setVerbose(true);
		}

		try {
			endpoint.start();
			Timer timer = new Timer(/*isDaemon*/ true);
			LifeProof lifeProof = new LifeProof(wsI, endpoint);
			endpoint.setLifeProof(lifeProof);
			
			if (wsI == 2) 
				lifeProof.getMediatorClient().imAlive();
			
			timer.schedule(lifeProof, /*delay*/ 0 * 1000, /*period*/ LifeProof.imAliveInterval * 1000);
			
			
			endpoint.awaitConnections();
		} finally {
			endpoint.stop();
		}

	}

}
