package org.komparator.supplier.ws;


/** Main class that starts the Supplier Web Service. */
public class SupplierApp {

	public static void main(String[] args) throws Exception {
		// Check arguments
		if (args.length < 1) {
			System.err.println("Argument(s) missing!");
			System.err.println("Usage: java " + SupplierApp.class.getName() + " wsURL OR wsURL wsName uddiURL");
			return;
		}
		String wsURL = args[0];
		
		SupplierEndpointManager endpoint = null;
		
		if(args.length == 1) {
			endpoint = new SupplierEndpointManager(wsURL);
		}
		else if(args.length == 3) {
			String wsName = args[1];
			String uddiURL = args[2];
			endpoint = new SupplierEndpointManager(wsURL, wsName, uddiURL);
		}
		
		
		try {
			endpoint.start();
			endpoint.awaitConnections();
		} finally {
			endpoint.stop();
		}

	}

}
