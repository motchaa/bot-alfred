package com.alfred.bot.infrastructure.waha;

public class SendMessageRequest {
    private String session;
    private String chatId;
    private String text;

    // Getters e Setters
    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}