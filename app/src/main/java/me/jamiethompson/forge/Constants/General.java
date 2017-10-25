package me.jamiethompson.forge.Constants;

/**
 * Created by jamie on 27/09/17.
 * General file holds global constants across the application
 */

public class General {
    final public static String FIRST_RUN = "first_run";
    final public static String ERROR_LOG = "error";

    final public static String BASE_URL = "http://api.guerrillamail.com/ajax.php";

    final public static String MAIL_DOMAIN = "guerrillamail.com";

    final public static int REQUEST_EMAILS = 0;
    final public static int REQUEST_ADDRESS = 1;
    final public static int REQUEST_SET_EMAIL = 2;

    final public static int EMAIL_REFRESH_DELAY = 10000;

    final public static int GENERATE_TAB = 0;
    final public static int STORE_TAB = 1;

    final public static String LOWERCASE_CHARACTERS = "abcdefghijklmnopqrstuvwxyz";
    final public static String SPECIAL_CHARACTERS = "~`!@#$%^&*()-_=+[{]}\\|;:\'\",<.>/?";
    final public static String NUMBER_CHARACTERS = "0123456789";
    final public static String UPPERCASE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
}
