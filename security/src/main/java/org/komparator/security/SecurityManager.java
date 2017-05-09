package org.komparator.security;

import java.util.HashSet;
import java.util.Set;

public class SecurityManager {
    private static final int MSG_TIMEOUT = 3;
    private static final String KEY_PASSWORD = "M6cggAUT";

    private static SecurityManager theManager = null;

    private String sender;
    private String destination;
    private Set<String> tokens;

    private SecurityManager() {
        this.sender = null;
        this.destination = null;
        this.tokens = new HashSet<>();
    }

    public static SecurityManager getInstance() {
        if (theManager == null)
            return new SecurityManager();
        return theManager;
    }

    public static int getMsgTimeout() {
        return MSG_TIMEOUT;
    }

    public String getDestination() {
    	return destination;
    }

    public void setDestination(String destination) {
    	this.destination = destination;
    }

    public String getSender() {
    	return sender;
    }

    public void setSender(String sender) {
    	this.sender = sender;
    }

    public String getPassword() {
    	return KEY_PASSWORD;
    }

    public Set<String> getTokens() {
    	return tokens;
    }
}
