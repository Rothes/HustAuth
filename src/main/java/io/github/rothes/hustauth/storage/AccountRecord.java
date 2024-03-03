package io.github.rothes.hustauth.storage;

public class AccountRecord {

    private final String userId;
    private final String password;
    private final String service;
    private final boolean encrypted;
    private final String display; // nullable

    public AccountRecord(String userId, String password, String service, boolean encrypted, String toString) {
        this.userId = userId;
        this.password = password;
        this.service = service;
        this.encrypted = encrypted;
        this.display = toString;
    }

    public AccountRecord(String userId, String password, String service, boolean encrypted) {
        this(userId, password, service, encrypted, null);
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public String getService() {
        return service;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    @Override
    public String toString() {
        return display != null ? display : userId;
    }

}
