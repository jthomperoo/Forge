package me.jamiethompson.forgeaccount;

import java.util.List;

/**
 * Created by jamie on 27/09/17.
 */

public interface EmailInterface
{
	void loadAddress(EmailAddress emailAddress);
	void loadEmails(List<EmailMessage> emails);
}
