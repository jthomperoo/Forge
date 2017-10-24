package me.jamiethompson.forge;

import java.util.List;

import me.jamiethompson.forge.Data.EmailAddress;
import me.jamiethompson.forge.Data.EmailMessage;

/**
 * Created by jamie on 27/09/17.
 * Used for interaction between an activity and the Mail Communicator, to load the address
 * or emails for an address when the communicator receives a result
 */

public interface EmailInterface {
    /**
     * Load a new email address returned by the API
     *
     * @param email the email address returned by the API
     */
    void loadAddress(EmailAddress email);

    /**
     * Load the emails sent to the email address returned by the API
     * @param emails a list of emails for the email address returned by the API
     */
    void loadEmails(List<EmailMessage> emails);
}
