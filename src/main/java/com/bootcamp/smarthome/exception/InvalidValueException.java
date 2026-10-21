package com.bootcamp.smarthome.exception;

public class InvalidValueException extends HomeAutomationException {
    private final String field;
    private final Object value;
    private final String constraint;
    public InvalidValueException(String field, Object value, String constraint) {
        super(String.format("Field %s has inccorent value (%s) from %s", field, value, constraint));

        this.field = field;
        this.value = value;
        this.constraint = constraint;
    }

    public String getField() {
        return field;
    }

    public Object getValue() {
        return value;
    }

    public String getConstraint() {
        return constraint;
    }
}
