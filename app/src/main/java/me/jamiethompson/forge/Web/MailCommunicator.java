package me.jamiethompson.forge.Web;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import me.jamiethompson.forge.Constants.Endpoints;
import me.jamiethompson.forge.Constants.General;
import me.jamiethompson.forge.Constants.ResponseKeys;
import me.jamiethompson.forge.Data.EmailAddress;
import me.jamiethompson.forge.Data.EmailMessage;
import me.jamiethompson.forge.Interfaces.EmailInterface;

/**
 * Created by jamie on 27/09/17.
 * Handles all interactions with the Guerrilla Mail API
 */

public class MailCommunicator implements VolleyInterface, Response.ErrorListener {
    // Email callback interface
    private EmailInterface IEmail;
    // Application context
    private Context appContext;
    // Volley callback interface
    private final VolleyInterface IVolley = this;
    RequestQueue queue;

    /**
     * @param IEmail     the class to callback to on response from the API
     * @param appContext the application context
     */
    public MailCommunicator(EmailInterface IEmail, Context appContext) {
        this.appContext = appContext;
        this.IEmail = IEmail;
        queue = Volley.newRequestQueue(appContext);
    }

    /**
     * Get and set up an email with the supplied string as the identifier before the @ sign
     *
     * @param email identifier before the @ sign
     */
    public void setEmail(String email) {
        // Set up parameters
        HashMap<String, String> parameters = new HashMap<>();
        // Add the email identifier as a parameter
        parameters.put(ResponseKeys.EMAIL_USER_PARAMETER_KEY, email);
        // Make the request to the API with the parameters and to the set email endpoint
        getRequest(Endpoints.SET_EMAIL, parameters, General.REQUEST_SET_EMAIL);
    }

    /**
     * Get the emails in the inbox for a supplied email address
     *
     * @param emailAddress the email address to retrieve the messages from
     */
    public void getEmails(EmailAddress emailAddress, EmailMessage latestEmail) {
        // The default email ID if no email loaded
        final String DEFAULT_EMAIL_ID = "0";
        // Set up parameters
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put(ResponseKeys.SID_PARAMETER_KEY, emailAddress.getSidToken());
        if (latestEmail != null) {
            // If there is an email loaded, use the latest email ID
            parameters.put(ResponseKeys.SEQUENCE_PARAMETER_KEY, latestEmail.getId());
        } else {
            // If there is no email loaded yet use the default email ID
            parameters.put(ResponseKeys.SEQUENCE_PARAMETER_KEY, DEFAULT_EMAIL_ID);
        }
        // Queue the GET request
        getRequest(Endpoints.GET_EMAILS, parameters, General.REQUEST_EMAILS);
    }

    /**
     * Generate and set up a random email from Guerrilla Mail
     */
    public void getAddress() {
        // Get request to get address endpoint with no parameters
        getRequest(Endpoints.GET_ADDRESS, new HashMap<String, String>(), General.REQUEST_ADDRESS);
    }

    /**
     * Fetches a specific email and all of it's details, e.g. email body
     *
     * @param email the email to request more information for
     */
    public void getEmail(EmailMessage email) {
        // Set up parameters
        HashMap<String, String> parameters = new HashMap<>();
        // Add the email identifier as a parameter
        parameters.put(ResponseKeys.SID_PARAMETER_KEY, email.getSid());
        // Add the email identifier as a parameter
        parameters.put(ResponseKeys.EMAIL_ID_KEY, email.getId());
        // Make the request to the API with the parameters and to the set email endpoint
        getRequest(Endpoints.GET_EMAIL, parameters, General.REQUEST_GET_EMAIL);
    }

    /**
     * Queues up the GET request to the Guerrilla Mail API
     *
     * @param endpoint   API endpoint
     * @param parameters request parameters
     * @param request    request type
     */
    private void getRequest(String endpoint, HashMap<String, String> parameters, final int request) {
        // Construct URL from endpoint and base URL
        String url = String.format("%s?f=%s", General.BASE_URL, endpoint);
        // Iterate through each parameter, adding the parameter key and value to the query params
        for (String key : parameters.keySet()) {
            url += String.format("&%s=%s", key, parameters.get(key));
        }
        // Create the Volley request, with callbacks to this class
        JsonObjectRequest get = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                IVolley.onResponse(request, response);
            }
        }, this);
        // Queue the request
        queue.add(get);
    }

    @Override
    public void onResponse(int request, JSONObject response) {
        // Depending on the request type
        switch (request) {
            case General.REQUEST_GET_EMAIL: {
                try {
                    // Set the format for hour and minute, 24 hour clock
                    DecimalFormat format = new DecimalFormat("00");
                    // Get current time that response is received
                    Calendar messageTime = Calendar.getInstance();
                    // Load the returned email
                    IEmail.loadEmail(new EmailMessage(false,
                            response.getString(ResponseKeys.MAIL_ID_KEY),
                            response.getString(ResponseKeys.SID_JSON_KEY),
                            response.getString(ResponseKeys.SUBJECT_JSON_KEY),
                            response.getString(ResponseKeys.BODY_JSON_KEY),
                            String.format("%s:%s", format.format(messageTime.get(Calendar.HOUR_OF_DAY)), format.format(messageTime.get(Calendar.MINUTE))),
                            response.getString(ResponseKeys.FROM_JSON_KEY)));
                } catch (JSONException e) {
                    // If there is an error, log it
                    Log.e(General.ERROR_LOG, e.getMessage());
                }
                break;
            }
            case General.REQUEST_ADDRESS:
            case General.REQUEST_SET_EMAIL: {
                // Request address and request set email have the same result, and same action
                try {
                    // Load the returned address
                    IEmail.loadAddress(new EmailAddress(response.getString(ResponseKeys.ADDRESS_JSON_KEY), response.getString(ResponseKeys.SID_JSON_KEY)));
                } catch (JSONException e) {
                    // If there is an error, log it
                    Log.e(General.ERROR_LOG, e.getMessage());
                }
                break;
            }
            case General.REQUEST_EMAILS: {
                // On email request
                try {
                    // Set the format for hour and minute, 24 hour clock
                    DecimalFormat format = new DecimalFormat("00");
                    // Get current time that response is received
                    Calendar messageTime = Calendar.getInstance();
                    List<EmailMessage> emails = new ArrayList<>();
                    // Get the emails in JSON format
                    JSONArray jsonEmails = response.getJSONArray(ResponseKeys.EMAILS_JSON_KEY);
                    // Iterate through each email
                    for (int i = 0; i < jsonEmails.length(); i++) {
                        JSONObject email = jsonEmails.getJSONObject(i);
                        // Create a new local email object
                        emails.add(new EmailMessage(false,
                                email.getString(ResponseKeys.MAIL_ID_KEY),
                                response.getString(ResponseKeys.SID_JSON_KEY),
                                email.getString(ResponseKeys.SUBJECT_JSON_KEY),
                                email.getString(ResponseKeys.EXCERPT_JSON_KEY),
                                String.format("%s:%s", format.format(messageTime.get(Calendar.HOUR_OF_DAY)), format.format(messageTime.get(Calendar.MINUTE))),
                                email.getString(ResponseKeys.FROM_JSON_KEY)));
                    }
                    // Callback to load the emails in the UI
                    IEmail.loadEmails(emails);
                } catch (JSONException e) {
                    // If there is an error, log it
                    Log.e(General.ERROR_LOG, e.getMessage());
                }
                break;
            }
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        // If there is an error, log it
        Log.e(General.ERROR_LOG, error.toString());
    }
}
