package me.jamiethompson.forge.Data;

/**
 * Created by jamie on 27/09/17.
 * Represents a Guerrilla Mail email address
 */

public class EmailAddress {
    // Email address string
    private String address;
    // Email SID Token from API
    private String sidToken;

    /**
     * @param address  email address value
     * @param sidToken SID Token from API
     */
    public EmailAddress(String address, String sidToken) {
        this.address = address;
        this.sidToken = sidToken;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public String getSidToken() {
        return sidToken;
    }
}
