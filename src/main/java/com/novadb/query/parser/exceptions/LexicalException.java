package com.novadb.query.parser.exceptions;

/**
 * Exception thrown when a lexical error occurs during tokenization.
 */
public class LexicalException extends RuntimeException {
    private final int line;
    private final int column;

    public LexicalException(String message, int line, int column) {
        super(String.format("Lexical Error at line %d, column %d: %s", line, column, message));
        this.line = line;
        this.column = column;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
}
