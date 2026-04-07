package com.sdcodebase.assignment.chat.application.ai;

public record AiMessage(Role role, String content) {

    public enum Role {
        USER, ASSISTANT, SYSTEM
    }

    public static AiMessage user(String content) {
        return new AiMessage(Role.USER, content);
    }

    public static AiMessage assistant(String content) {
        return new AiMessage(Role.ASSISTANT, content);
    }

    public static AiMessage system(String content) {
        return new AiMessage(Role.SYSTEM, content);
    }
}
