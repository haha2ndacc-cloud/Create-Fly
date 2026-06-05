package com.zurrtum.create.client.flywheel.backend.glsl.error.lines;

public enum Divider {
    BAR(" | "), ARROW("-> "), EQUALS(" = ");

    private final String s;

    Divider(String s) {
        this.s = s;
    }

    public String toString() {
        return s;
    }
}
