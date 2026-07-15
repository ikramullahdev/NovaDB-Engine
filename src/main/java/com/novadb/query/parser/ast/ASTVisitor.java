package com.novadb.query.parser.ast;

/**
 * Interface for traversing the AST using the Visitor pattern.
 */
public interface ASTVisitor {
    void visit(SelectStatement node);
    void visit(InsertStatement node);
    void visit(UpdateStatement node);
    void visit(DeleteStatement node);
    void visit(CreateTableStatement node);
    void visit(DropTableStatement node);
    // Add more visit methods as needed for expressions, etc.
}
