package com.avygeil.bprnt.module.overwatch;

public class OverwatchParserException extends Exception {

    private static final long serialVersionUID = 7718828512143293558L;

    private final int line;

    public OverwatchParserException(String message, int line) {
        super(message);
        this.line = line;
    }

    public int getLine() {
        return line;
    }

}
