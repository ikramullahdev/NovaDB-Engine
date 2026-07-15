package com.novadb.query.parser;

import com.novadb.query.parser.ast.ASTNode;
import com.novadb.query.parser.ast.SelectStatement;
import com.novadb.query.parser.exceptions.SyntaxException;
import java.util.Collections;

/**
 * Hand-written Recursive-Descent Parser for NovaDB SQL.
 */
public class Parser {
    private final Lexer lexer;
    private Token currentToken;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        this.currentToken = lexer.nextToken();
    }

    public ASTNode parse() {
        if (match(TokenType.SELECT)) {
            return parseSelect();
        }
        throw new SyntaxException("Unsupported statement starting with " + currentToken.getType(), 
                                   currentToken.getLine(), currentToken.getColumn());
    }

    private SelectStatement parseSelect() {
        consume(TokenType.SELECT);
        boolean distinct = false;
        if (match(TokenType.DISTINCT)) {
            consume(TokenType.DISTINCT);
            distinct = true;
        }
        
        // Placeholder for full SELECT parsing logic
        return new SelectStatement(distinct, Collections.emptyList(), null, null);
    }

    private boolean match(TokenType type) {
        return currentToken.getType() == type;
    }

    private void consume(TokenType type) {
        if (match(type)) {
            currentToken = lexer.nextToken();
        } else {
            throw new SyntaxException(currentToken.getLine(), currentToken.getColumn(), 
                                       currentToken.getType(), Collections.singletonList(type));
        }
    }
}
