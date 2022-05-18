package me.thegiggitybyte.wumpuslink.error;

public class ConfigurationValueEmptyError extends Error {
    public ConfigurationValueEmptyError(String fieldName) {
        super("Value of " + fieldName + " cannot be empty");
    }
}