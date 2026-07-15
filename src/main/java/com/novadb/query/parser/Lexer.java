package com.novadb.query.parser;

import com.novadb.query.parser.exceptions.LexicalException;
import java.util.ArrayList;
import java.util.List;

/**
 * Hand-written Lexer for NovaDB SQL.
 */
public class Lexer {
    private final String input;
    private int position = 0;
    private int line = 1;
    private int column = 1;

    public Lexer(String input) {
        this.input = input;
    }

    public Token nextToken() {
        skipWhitespace();
        if (position >= input.length()) {
            return new Token(TokenType.EOF, "", line, column);
        }
        
        // Initial placeholder implementation
        char current = input.charAt(position);
        if (Character.isLetter(current)) {
            return readIdentifierOrKeyword();
        }
        
        throw new LexicalException("Unexpected character: " + current, line, column);
    }

    private Token readIdentifierOrKeyword() {
        StringBuilder sb = new StringBuilder();
        int startCol = column;
        while (position < input.length() && (Character.isLetterOrDigit(input.charAt(position)) || input.charAt(position) == '_')) {
            sb.append(input.charAt(position));
            advance();
        }
        String lexeme = sb.toString();
        TokenType type = getKeywordType(lexeme);
        return new Token(type, lexeme, line, startCol);
    }

    private TokenType getKeywordType(String lexeme) {
        try {
            return TokenType.valueOf(lexeme.toUpperCase());
        } catch (IllegalArgumentException e) {
            return TokenType.IDENTIFIER;
        }
    }

    private void skipWhitespace() {
        while (position < input.length() && Character.isWhitespace(input.charAt(position))) {
            if (input.charAt(position) == '\n') {
                line++;
                column = 1;
            } else {
                column++;
            }
            position++;
        }
    }

    private void advance() {
        position++;
        column++;
    }
}
