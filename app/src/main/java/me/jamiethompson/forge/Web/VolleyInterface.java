package me.jamiethompson.forge.Web;

import org.json.JSONObject;

/**
 * Created by jamie on 27/09/17.
 * Used in the Mail Communicator to receive results from web requests
 */

interface VolleyInterface {
    /**
     * Called when a web request has returned a result
     *
     * @param request  the request type
     * @param response the JSON response to the request
     */
    void onResponse(int request, JSONObject response);
}
