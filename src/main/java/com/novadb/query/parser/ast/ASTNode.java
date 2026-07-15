package com.novadb.query.parser.ast;

/**
 * Base interface for all nodes in the Abstract Syntax Tree (AST).
 */
public interface ASTNode {
    void accept(ASTVisitor visitor);
}
