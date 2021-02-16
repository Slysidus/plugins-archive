package net.lightning.api.commands;

import lombok.Getter;

@Getter
public class CommandExecutionException extends RuntimeException {

    private final Type type;

    public CommandExecutionException(Type type, String message) {
        super(message);
        this.type = type;
    }

    public enum Type {

        ERROR,
        SEVERE,
        DENIED,
        FAIL,
        OTHER

    }

}
