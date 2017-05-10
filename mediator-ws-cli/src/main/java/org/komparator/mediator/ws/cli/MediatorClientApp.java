package org.komparator.mediator.ws.cli;

import org.komparator.mediator.ws.ShoppingResultView;

public class MediatorClientApp {

    public static void main(String[] args) throws Exception {
        // Check arguments
        if (args.length == 0) {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: java " + MediatorClientApp.class.getName()
                    + " wsURL OR uddiURL wsName");
            return;
        }
        String uddiURL = null;
        String wsName = null;
        String wsURL = null;
        if (args.length == 1) {
            wsURL = args[0];
        } else if (args.length >= 2) {
            uddiURL = args[0];
            wsName = args[1];
        }

        // Create client
        MediatorClient client = null;

        if (wsURL != null) {
            System.out.printf("Creating client for server at %s%n", wsURL);
            client = new MediatorClient(wsURL);
        } else if (uddiURL != null) {
            System.out.printf("Creating client using UDDI at %s for server with name %s%n",
                uddiURL, wsName);
            client = new MediatorClient(uddiURL, wsName);
        }

        // the following remote invocations are just basic examples
        // the actual tests are made using JUnit

        System.out.println("Invoke buyCart()...");
        try {
            ShoppingResultView srv = client.buyCart("client", "4012888888881881");
            System.out.println(srv.getResult());
        } catch (Exception e) {
            System.err.println("Ignoring exception: " + e);
        }
    }
}
