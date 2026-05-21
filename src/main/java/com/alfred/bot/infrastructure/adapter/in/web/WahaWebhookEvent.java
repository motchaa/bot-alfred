package com.alfred.bot.infrastructure.adapter.in.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WahaWebhookEvent {
    private String event;
    private String session;
    private MessagePayload payload;

    // Getters e Setters
    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public MessagePayload getPayload() {
        return payload;
    }

    public void setPayload(MessagePayload payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "WahaWebhookEvent{" +
                "event='" + event + '\'' +
                ", session='" + session + '\'' +
                ", payload=" + payload +
                '}';
    }
}