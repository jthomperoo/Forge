package me.jamiethompson.forge.Data;

/**
 * Created by jamie on 27/09/17.
 * Represents an email message
 */

public class EmailMessage {
    // Has the email been read
    private boolean read;
    // Email ID
    private String id;
    // Email subject
    private String subject;
    // Email body
    private String body;
    // Email time
    private String time;
    // Email sender address
    private String from;

    /**
     * @param read    has the email been read
     * @param id      email ID
     * @param subject email subject
     * @param body    email body
     * @param time    email receiving time
     * @param from    email sender address
     */
    public EmailMessage(boolean read, String id, String subject, String body, String time, String from) {
        this.read = read;
        this.id = id;
        this.subject = subject;
        this.body = body;
        this.time = time;
        this.from = from;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

}
