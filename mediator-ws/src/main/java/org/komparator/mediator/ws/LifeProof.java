package org.komparator.mediator.ws;

import java.time.LocalDateTime;
import java.util.TimerTask;

import org.komparator.mediator.ws.cli.MediatorClient;
import org.komparator.mediator.ws.cli.MediatorClientException;

public class LifeProof extends TimerTask{

	private boolean isPrimary = false;
	private boolean secondaryExists = false;
	private MediatorClient mediatorClient = null;
	private MediatorEndpointManager mediatorEndpoint = null;
	
	public final static int imAliveInterval = 5;
	public final static int maxWaitTime = 6;
	
	public MediatorClient getMediatorClient() {
		return mediatorClient;
	}
	
	public void setSecondaryExists(boolean b) {
		secondaryExists = b;
	}
	
	public boolean getSecondaryExists() {
		return secondaryExists;
	}

    public LifeProof(int mediatorID, MediatorEndpointManager endpoint) throws MediatorClientException {
        this.isPrimary = mediatorID == 1;
        if (mediatorID != 0) {
        	this.mediatorClient = new MediatorClient("http://localhost:807" +(isPrimary ? "2" : "1") + "/mediator-ws/endpoint");
        	if (!isPrimary) 
        		this.mediatorEndpoint = endpoint;
        }
        	
    }
    
    public LifeProof(int mediatorID, String wsURL) throws MediatorClientException {
        this.isPrimary = mediatorID == 1;
        this.mediatorClient = new MediatorClient(wsURL);
    }
	
	@Override
	public void run() {
		if(mediatorClient != null) {
			if (isPrimary) {
				if (secondaryExists)
					mediatorClient.imAlive();
			}
			else {
				LocalDateTime timestamp = mediatorEndpoint.getImAliveHistory();

				if (timestamp != null && timestamp.plusSeconds(maxWaitTime).isBefore(LocalDateTime.now())) {
					System.out.println(timestamp.toString());
					System.out.println("Primary mediator is probably dead");
					try {
						mediatorEndpoint.setIsPrimary(true);
						isPrimary = true;
						mediatorEndpoint.publishToUDDI();
					} catch (Exception e) {
						System.out.println("Couldn't publish to UDDI" + e.getMessage());
					}
				}
			}
		}
	}
}
