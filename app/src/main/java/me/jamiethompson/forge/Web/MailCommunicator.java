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
 */

public class MailCommunicator implements VolleyInterface, Response.ErrorListener {
    final private int EMAIL_SEQUENCE_START = 0;
    private EmailInterface IEmail;
    private Context appContext;
    final VolleyInterface IVolley = this;

    public MailCommunicator(EmailInterface IEmail, Context appContext) {
        this.appContext = appContext;
        this.IEmail = IEmail;
    }

    public void setEmail(String email) {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put(Constants.EMAIL_USER_PARAMETER_KEY, email);
        getRequest(Constants.SET_EMAIL, parameters, Constants.REQUEST_SET_EMAIL);
    }

    public void getEmails(EmailAddress emailAddress) {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put(Constants.SID_PARAMETER_KEY, emailAddress.getSidToken());
        parameters.put(Constants.SEQUENCE_PARAMETER_KEY, String.valueOf(EMAIL_SEQUENCE_START));
        getRequest(Constants.GET_EMAILS, parameters, Constants.REQUEST_EMAILS);
    }

    public void getAddress() {
        HashMap<String, String> parameters = new HashMap<>();
        getRequest(Constants.GET_ADDRESS, parameters, Constants.REQUEST_ADDRESS);
    }

    public void getRequest(String endpoint, HashMap<String, String> parameters, final int request) {
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
                    Log.e(Constants.ERROR_LOG, e.getMessage().toString());
                }
                break;
            }
            case Constants.REQUEST_EMAILS: {
                try {
                    Calendar messageTime = Calendar.getInstance();
                    List<EmailMessage> emails = new ArrayList<>();
                    JSONArray jsonEmails = response.getJSONArray(Constants.EMAILS_JSON_KEY);
                    DecimalFormat mFormat = new DecimalFormat("00");
                    for (int i = 0; i < jsonEmails.length(); i++) {
                        JSONObject email = jsonEmails.getJSONObject(i);
                        emails.add(new EmailMessage(false,
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
