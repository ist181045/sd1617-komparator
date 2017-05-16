package org.komparator.mediator.ws.cli;

public class MediatorClientApp {

    public static void main(String[] args) throws Exception {
        // Check arguments
        if (args.length == 0) {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: java " + MediatorClientApp.class.getName()
                    + " wsURL OR uddiURL wsName");
            return;
        }

        String uddiUrl = null;
        String wsName = null;
        String wsUrl = null;

        int connectTimeout = 3;
        int receiveTimeout = 5;

        if (args.length == 1) {
            wsUrl = args[0];
        } else if (args.length >= 2) {
            uddiUrl = args[0];
            wsName = args[1];
            if (args.length > 2)
                connectTimeout = Integer.parseInt(args[2]);
            if (args.length > 3)
                receiveTimeout = Integer.parseInt(args[3]);
        }

        // Create client
        MediatorClient client = null;

        if (wsUrl != null) {
            System.out.printf("Creating client for server at %s%n", wsUrl);
            client = new MediatorClient(wsUrl);
        } else if (uddiUrl != null) {
            System.out.printf("Creating client using UDDI at %s for server "
                    + "with name %s%n", uddiUrl, wsName);
            client = new MediatorClient(uddiUrl, wsName,
                    connectTimeout, receiveTimeout);
        }
        
        System.out.println("Invoke ping()...");
        String result = client.ping("client");
        System.out.println(result);
    }
}
