package me.jamiethompson.forgeaccount.Data;

import java.util.Calendar;
import java.util.UUID;

/**
 * Created by jamie on 27/09/17.
 */

public class ForgeAccount
{
	private String accountName;
	private String firstName;
	private String middleName;
	private String lastName;

	private String username;
	private EmailAddress email;
	private String password;
	private Calendar dateOfBirth;

	private UUID id;

	public ForgeAccount()
	{
	}

	public String getAccountName()
	{
		return accountName;
	}

	public void setAccountName(String accountName)
	{
		this.accountName = accountName;
	}

	public String getFirstName()
	{
		return firstName;
	}

	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}

	public String getMiddleName()
	{
		return middleName;
	}

	public void setMiddleName(String middleName)
	{
		this.middleName = middleName;
	}

	public String getLastName()
	{
		return lastName;
	}

	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public EmailAddress getEmail()
	{
		return email;
	}

	public void setEmail(EmailAddress email)
	{
		this.email = email;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public Calendar getDateOfBirth()
	{
		return dateOfBirth;
	}

	public void setDateOfBirth(Calendar dateOfBirth)
	{
		this.dateOfBirth = dateOfBirth;
	}

	public UUID getId()
	{
		return id;
	}

	public void setId(UUID id)
	{
		this.id = id;
	}
}

