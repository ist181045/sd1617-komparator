package org.komparator.mediator.ws;

import java.net.URL;
import java.time.Instant;
import java.util.TimerTask;

import org.komparator.mediator.ws.cli.MediatorClient;
import org.komparator.mediator.ws.cli.MediatorClientException;

public class LifeProof extends TimerTask{

	private boolean isPrimary = false;
	private boolean secondaryExists = false;

	private int interval;

	private Instant lastImAlive;

	private MediatorClient mediatorClient = null;
	private MediatorEndpointManager mediatorEndpoint = null;
	
	public MediatorClient getMediatorClient() {
		return mediatorClient;
	}
	
	public boolean isPrimary() {
		return isPrimary;
	}

	public boolean secondaryExists() {
		return secondaryExists;
	}

	public void setSecondaryExists(boolean secondaryExists) {
		this.secondaryExists = secondaryExists;
	}

	public Instant getLastImAlive() {
		return lastImAlive;
	}

	public void setLastImAlive(Instant lastImAlive) {
		this.lastImAlive = lastImAlive;
	}

    public LifeProof(MediatorEndpointManager endpoint, URL wsUrl, int wsId,
			int interval) throws MediatorClientException {
        this.isPrimary = wsId == 1;
        this.interval = interval;

        if (wsId != 0) {
        	this.mediatorClient = new MediatorClient(
        			String.format("%s://%s:%d%s",
							wsUrl.getProtocol(), wsUrl.getHost(),
							(wsUrl.getPort() / 10) * 10 + (wsId % 2) + 1,
							wsUrl.getPath()));
        	if (!isPrimary)
        		this.mediatorEndpoint = endpoint;
        }
    }

	@Override
	public void run() {
		if (isPrimary) {
			if (secondaryExists)
				mediatorClient.imAlive();
		} else {
			if (lastImAlive != null &&
					lastImAlive.plusSeconds(interval).isBefore(Instant.now())) {
				System.out.print(lastImAlive.toString());
				System.out.println(": Primary mediator is probably dead");
				try {
					isPrimary = true;
					mediatorEndpoint.publishToUDDI();
				} catch (Exception e) {
					System.out.println("Couldn't publish to UDDI" + e.getMessage());
				}
			}
		}
	}
}
