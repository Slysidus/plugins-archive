package net.lightning.core.loader;

import lombok.Getter;

@Getter
public class ConfigurationModelLoadException extends Exception {

    private final Class<? extends ConfigurationModel> targetClass;
    private final ErrorType errorType;

    public ConfigurationModelLoadException(Class<? extends ConfigurationModel> targetClass, ErrorType errorType, String message) {
        super(message);
        this.targetClass = targetClass;
        this.errorType = errorType;
    }

    public enum ErrorType {

        BAD_CONSTRUCTOR,
        BAD_BUILDER_METHOD,
        UNEXPECTED_BEHAVIOR,
        UNKNOWN_FIELD,
        INVALID_ADAPTER,
        NULL,

    }

}
