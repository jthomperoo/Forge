package me.jamiethompson.forgeaccount;

import java.util.List;

import me.jamiethompson.forgeaccount.Data.EmailAddress;
import me.jamiethompson.forgeaccount.Data.EmailMessage;

/**
 * Created by jamie on 27/09/17.
 */

public interface EmailInterface {
    void loadAddress(EmailAddress emailAddress);

    void loadEmails(List<EmailMessage> emails);
}
