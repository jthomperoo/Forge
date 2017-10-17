package me.jamiethompson.forge;

import java.util.List;

import me.jamiethompson.forge.Data.EmailAddress;
import me.jamiethompson.forge.Data.EmailMessage;

/**
 * Created by jamie on 27/09/17.
 */

public interface EmailInterface {
    void loadAddress(EmailAddress emailAddress);

    void loadEmails(List<EmailMessage> emails);
}
