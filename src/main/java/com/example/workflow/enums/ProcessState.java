package com.example.workflow.enums;

public enum ProcessState {
    ACTIVE("ACTIVE");

    private final String state;

    public String getState() {
        return this.state;
    }

    ProcessState(final String state) {
        this.state = state;
    }
}