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

import me.jamiethompson.forge.Constants;
import me.jamiethompson.forge.Data.EmailAddress;
import me.jamiethompson.forge.Data.EmailMessage;
import me.jamiethompson.forge.EmailInterface;

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

    /**
     * @param IEmail     the class to callback to on response from the API
     * @param appContext the application context
     */
    public MailCommunicator(EmailInterface IEmail, Context appContext) {
        this.appContext = appContext;
        this.IEmail = IEmail;
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
        parameters.put(Constants.EMAIL_USER_PARAMETER_KEY, email);
        // Make the request to the API with the parameters and to the set email endpoint
        getRequest(Constants.SET_EMAIL, parameters, Constants.REQUEST_SET_EMAIL);
    }

    /**
     * Get the emails in the inbox for a supplied email address
     *
     * @param emailAddress the email address to retrieve the messages from
     */
    public void getEmails(EmailAddress emailAddress, EmailMessage latestEmail) {
        // The default email ID if no email loaded
        final String DEFAULT_EMAIL_ID = "0";
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put(Constants.SID_PARAMETER_KEY, emailAddress.getSidToken());
        if (latestEmail != null) {
            parameters.put(Constants.SEQUENCE_PARAMETER_KEY, latestEmail.getId());
        } else {
            parameters.put(Constants.SEQUENCE_PARAMETER_KEY, DEFAULT_EMAIL_ID);
        }
        getRequest(Constants.GET_EMAILS, parameters, Constants.REQUEST_EMAILS);
    }

    /**
     * Generate and set up a random email from Guerrilla Mail
     */
    public void getAddress() {
        HashMap<String, String> parameters = new HashMap<>();
        getRequest(Constants.GET_ADDRESS, parameters, Constants.REQUEST_ADDRESS);
    }

    /**
     * @param endpoint
     * @param parameters
     * @param request
     */
    private void getRequest(String endpoint, HashMap<String, String> parameters, final int request) {
        RequestQueue queue = Volley.newRequestQueue(appContext);
        String url = String.format("%s?f=%s", Constants.BASE_URL, endpoint);
        for (String key : parameters.keySet()) {
            url += String.format("&%s=%s", key, parameters.get(key));
        }
        JsonObjectRequest get = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                IVolley.onResponse(request, response);
            }
        }, this);
        queue.add(get);
    }

    @Override
    public void onResponse(int request, JSONObject response) {
        switch (request) {
            case Constants.REQUEST_ADDRESS:
            case Constants.REQUEST_SET_EMAIL: {
                try {
                    IEmail.loadAddress(new EmailAddress(response.getString(Constants.ADDRESS_JSON_KEY), response.getString(Constants.SID_JSON_KEY)));
                } catch (JSONException e) {
                    Log.e(Constants.ERROR_LOG, e.getMessage());
                }
                break;
            }
            case Constants.REQUEST_EMAILS: {
                try {
                    Log.d("mega", response.toString());
                    Calendar messageTime = Calendar.getInstance();
                    List<EmailMessage> emails = new ArrayList<>();
                    JSONArray jsonEmails = response.getJSONArray(Constants.EMAILS_JSON_KEY);
                    DecimalFormat mFormat = new DecimalFormat("00");
                    for (int i = 0; i < jsonEmails.length(); i++) {
                        JSONObject email = jsonEmails.getJSONObject(i);
                        emails.add(new EmailMessage(false,
                                email.getString(Constants.EMAIL_ID_KEY),
                                email.getString(Constants.SUBJECT_JSON_KEY),
                                email.getString(Constants.BODY_JSON_KEY),
                                String.format("%s:%s", mFormat.format(messageTime.get(Calendar.HOUR_OF_DAY)), mFormat.format(messageTime.get(Calendar.MINUTE))),
                                email.getString(Constants.FROM_JSON_KEY)));
                    }
                    IEmail.loadEmails(emails);
                } catch (JSONException e) {
                    Log.e(Constants.ERROR_LOG, e.getMessage().toString());
                }
                break;
            }
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.e(Constants.ERROR_LOG, error.toString());
    }
}
