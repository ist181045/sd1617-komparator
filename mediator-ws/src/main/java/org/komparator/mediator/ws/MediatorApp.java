package org.komparator.mediator.ws;

import java.net.URL;
import java.util.Timer;

public class MediatorApp {

	public static void main(String[] args) throws Exception {
		// Check arguments
		if (args.length < 1 || args.length == 3) {
			System.err.println("Argument(s) missing!");
			System.err.println("Usage: java " + MediatorApp.class.getName()
                    + " <wsURL> [interval] OR"
                    + " <uddiURL> <wsName> <wsURL> [interval]");
			return;
		}

		String uddiUrl = null;
		String wsName = null;
		String wsUrl = null;

        int wsId = 1;
        int interval = 5;

		// Create server implementation object, according to options
        MediatorEndpointManager endpoint = null;
        if (args.length < 3) {
            wsUrl = args[0];

            if (args.length == 2 && args[1] != null) {
                interval = Integer.parseInt(args[1]);
            }

            endpoint = new MediatorEndpointManager(wsUrl);
        } else {
            uddiUrl = args[0];
            wsName = args[1];
            wsUrl = args[2];
            wsId = new URL(wsUrl).getPort() % 10;

            if (args.length == 2 && args[1] != null) {
                interval = Integer.parseInt(args[1]);
            }

            endpoint = new MediatorEndpointManager(uddiUrl, wsName, wsUrl);
            endpoint.setVerbose(true);
        }

        LifeProof lifeProof =
                new LifeProof(endpoint, new URL(wsUrl), wsId, interval);
        endpoint.setLifeProof(lifeProof);

        try {
            endpoint.start();

            Timer timer = new Timer(true);
            timer.schedule(lifeProof, 0,
                    (interval + (wsId + 1) % 2) * 1000);

            if (!lifeProof.isPrimary()) {
                lifeProof.getMediatorClient().imAlive();
            }

            endpoint.awaitConnections();
		} finally {
			endpoint.stop();
			System.exit(0);
		}

	}

}
