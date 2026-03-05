package com.alfred.bot.infrastructure.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MessagePayload {
    private String id;
    private Long timestamp;
    private String from;
    private String body;

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "MessagePayload{" +
                "id='" + id + '\'' +
                ", timestamp=" + timestamp +
                ", from='" + from + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}