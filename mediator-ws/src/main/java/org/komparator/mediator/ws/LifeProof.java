package org.komparator.mediator.ws;

import java.util.TimerTask;

import org.komparator.mediator.ws.cli.MediatorClient;
import org.komparator.mediator.ws.cli.MediatorClientException;

public class LifeProof extends TimerTask{

	String mediatorID = null;
	MediatorClient mediatorClient = null;

    public LifeProof(String mediatorID) throws MediatorClientException {
        this.mediatorID = mediatorID;
        if (mediatorID != null) {
        	this.mediatorClient = new MediatorClient("http://localhost:807" +(mediatorID.equals("1") ? "2" : "1") + "/mediator-ws/endpoint");
        }
        	
    }
    
    public LifeProof(String argument, String wsURL) throws MediatorClientException {
        this.mediatorID = argument;
        this.mediatorClient = new MediatorClient(wsURL);
    }
	
	@Override
	public void run() {
		if (mediatorID != null && mediatorID.equals("1") && mediatorClient != null) {
			mediatorClient.imAlive();
		}
		
	}
	
}
