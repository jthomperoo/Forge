package me.jamiethompson.forge.Data;

/**
 * Created by jamie on 27/09/17.
 */

public class EmailAddress {
    private String address;
    private String SidToken;

    public EmailAddress(String address, String sidToken) {
        this.address = address;
        SidToken = sidToken;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSidToken() {
        return SidToken;
    }

    public void setSidToken(String sidToken) {
        SidToken = sidToken;
    }
}
