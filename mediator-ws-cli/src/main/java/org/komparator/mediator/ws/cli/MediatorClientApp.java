package org.komparator.mediator.ws.cli;

import org.komparator.mediator.ws.ShoppingResultView;

public class MediatorClientApp {

    public static void main(String[] args) throws Exception {
        // Check arguments
        if (args.length == 0) {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: java " + MediatorClientApp.class.getName()
                    + " wsURL [connect receive]"
                    + " OR uddiURL wsName [connect receive]");
            return;
        }

        String uddiUrl = null;
        String wsName = null;
        String wsUrl = null;

        int connectTimeout = 5;
        int receiveTimeout = 8;

        if (args.length == 1 || args.length == 3) {
            wsUrl = args[0];
            if (args.length > 1) {
                connectTimeout = Integer.parseInt(args[1]);
                receiveTimeout = Integer.parseInt(args[2]);
            }
        } else {
            uddiUrl = args[0];
            wsName = args[1];
            if (args.length > 2) {
                connectTimeout = Integer.parseInt(args[2]);
                receiveTimeout = Integer.parseInt(args[3]);
            }
        }

        // Create client
        MediatorClient client = null;

        if (wsUrl != null) {
            System.out.printf("Creating client for server at %s%n", wsUrl);
            client = new MediatorClient(wsUrl, connectTimeout, receiveTimeout);
        } else if (uddiUrl != null) {
            System.out.printf("Creating client using UDDI at %s for server "
                    + "with name %s%n", uddiUrl, wsName);
            client = new MediatorClient(uddiUrl, wsName,
                    connectTimeout, receiveTimeout);
        }
        
        System.out.println("Invoke buyCart()...");
        ShoppingResultView srv = client.buyCart("id", "4018888888881812");
        System.out.println(srv.getId());
    }
}
