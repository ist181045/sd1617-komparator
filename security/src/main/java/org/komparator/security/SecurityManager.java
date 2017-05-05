package org.komparator.security;

import java.util.HashSet;
import java.util.Set;

public class SecurityManager {
    private static final SecurityManager instance = new SecurityManager();

    private static final String KEY_PASSWORD = "M6cggAUT";
    
    private static final int MAX_TIMEOUT = 3;

    private String destination = null;
    private String sender = null;
    
    private Set<String> tokens = new HashSet<String>();


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
    
    public String getPassword() {
    	return KEY_PASSWORD;
    }
    
    public static int getMaxTimeout() {
    	return MAX_TIMEOUT;
    }
    
    public void addToken(String token) {
    	tokens.add(token);
    }
    public boolean checkToken(String token) {
    	return token != null && tokens.contains(token);
    }
    
    public Set<String> getTokens() {
    	return tokens;
    }
}
