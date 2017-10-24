package me.jamiethompson.forge.Data;

/**
 * Created by jamie on 27/09/17.
 */

public class EmailMessage {
    private boolean read;
    private String id;
    private String subject;
    private String body;
    private String time;
    private String from;

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

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
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
