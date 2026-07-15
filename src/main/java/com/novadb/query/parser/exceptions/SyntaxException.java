package com.novadb.query.parser.exceptions;

import com.novadb.query.parser.TokenType;
import java.util.List;

/**
 * Exception thrown when a syntax error occurs during parsing.
 */
public class SyntaxException extends RuntimeException {
    private final int line;
    private final int column;
    private final TokenType foundType;
    private final List<TokenType> expectedTypes;

    public SyntaxException(String message, int line, int column) {
        super(String.format("Syntax Error at line %d, column %d: %s", line, column, message));
        this.line = line;
        this.column = column;
        this.foundType = null;
        this.expectedTypes = null;
    }

    public SyntaxException(int line, int column, TokenType foundType, List<TokenType> expectedTypes) {
        super(String.format("Syntax Error at line %d, column %d: Expected %s but found %s",
                line, column, expectedTypes, foundType));
        this.line = line;
        this.column = column;
        this.foundType = foundType;
        this.expectedTypes = expectedTypes;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public TokenType getFoundType() {
        return foundType;
    }

    public List<TokenType> getExpectedTypes() {
        return expectedTypes;
    }
}
