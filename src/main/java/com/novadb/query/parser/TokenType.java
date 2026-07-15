package com.novadb.query.parser;

/**
 * Enum defining all possible token types recognized by the Lexer.
 */
public enum TokenType {
    // Keywords
    SELECT, DISTINCT, FROM, WHERE, AS, ORDER, BY, ASC, DESC, LIMIT, OFFSET,
    INSERT, INTO, VALUES, UPDATE, SET, DELETE, CREATE, TABLE, DROP,
    INT, VARCHAR, BOOLEAN, DOUBLE, PRIMARY, KEY, NOT, NULL, UNIQUE,
    AND, OR, LIKE, IN, TRUE, FALSE, IS,

    // Operators
    EQ, NEQ, LT, GT, LTE, GTE,
    PLUS, MINUS, ASTERISK, SLASH,

    // Punctuation
    COMMA, SEMICOLON, LPAREN, RPAREN,

    // Literals and Identifiers
    IDENTIFIER,
    LITERAL_STRING,
    LITERAL_INT,
    LITERAL_DECIMAL,

    // Special
    EOF,
    UNKNOWN
}
