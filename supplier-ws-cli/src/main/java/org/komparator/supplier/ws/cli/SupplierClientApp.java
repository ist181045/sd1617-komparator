package org.komparator.supplier.ws.cli;

/** Main class that starts the Supplier Web Service client. */
public class SupplierClientApp {

	public static void main(String[] args) throws Exception {
		// Check arguments
		if (args.length < 1) {
			System.err.println("Argument(s) missing!");
			System.err.println("Usage: java " + SupplierClientApp.class.getName() + " wsURL OR uddiURL wsName");
			return;
		}
		
		SupplierClient client = null;
		
		if(args.length == 1) {
			String wsURL = args[0];
			client = new SupplierClient(wsURL);
		}
		else if(args.length == 2) {
			String uddiURL = args[0];
			String wsName = args[1];
			client = new SupplierClient(uddiURL, wsName);
		}

		// the following remote invocations are just basic examples
		// the actual tests are made using JUnit

		System.out.println("Invoke ping()...");
		String result = client.ping("client");
		System.out.print("Result: ");
		System.out.println(result);
	}

}
