package org.komparator.security;

public class SecurityManager {
    private static final SecurityManager instance = new SecurityManager();

    private static final String KEY_PASSWORD = "M6cggAUT";
    
    private String destination = null;
    private String sender = null;

    private SecurityManager() {
    }

    public static SecurityManager getInstance() {
        return instance;
    }
    
    public String getDestination() {
    	return destination;
    }
    
    public void setDestination(String arg) {
    	destination = arg;
    }
    
    public String getSender() {
    	return sender;
    }
    
    public void setSender(String arg) {
    	sender = arg;
    }
}
