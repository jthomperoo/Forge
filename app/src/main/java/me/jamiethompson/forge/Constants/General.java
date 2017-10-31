package me.jamiethompson.forge.Constants;

/**
 * Created by jamie on 27/09/17.
 * General file holds global constants across the application
 */

public class General {
    // Key to check if this is the first time the app has been run using shared preferences
    final public static String FIRST_RUN = "first_run";
    // Error log key
    final public static String ERROR_LOG = "error";
    // Base API URL
    final public static String BASE_URL = "http://api.guerrillamail.com/ajax.php";
    // API Request types
    final public static int REQUEST_EMAILS = 0;
    final public static int REQUEST_ADDRESS = 1;
    final public static int REQUEST_SET_EMAIL = 2;
    final public static int REQUEST_GET_EMAIL = 3;
    // API refresh delay, in ms, will check every x milliseconds
    final public static int EMAIL_REFRESH_DELAY = 10000;
    // Fragment tab types
    final public static int GENERATE_TAB = 0;
    final public static int STORE_TAB = 1;
    // Character sets
    final public static String LOWERCASE_CHARACTERS = "abcdefghijklmnopqrstuvwxyz";
    final public static String SPECIAL_CHARACTERS = "~`!@#$%^&*()-_=+[{]}\\|;:\'\",<.>/?";
    final public static String NUMBER_CHARACTERS = "0123456789";
    final public static String UPPERCASE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
}
