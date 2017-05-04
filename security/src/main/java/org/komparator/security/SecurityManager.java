package org.komparator.security;

public class SecurityManager {
    private static final SecurityManager instance = new SecurityManager();

    private static final String KEY_PASSWORD = "M6cggAUT";

    private SecurityManager() {
    }

    public static SecurityManager getInstance() {
        return instance;
    }
}
