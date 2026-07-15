package com.novadb.query.parser.ast;

import java.util.List;

/**
 * Represents a SELECT statement in the AST.
 */
public class SelectStatement extends Statement {
    private final boolean distinct;
    // For simplicity in scaffolding, using placeholders for Expression and TableExpression
    private final List<Object> selectList; 
    private final Object fromClause;
    private final Object whereClause;

    public SelectStatement(boolean distinct, List<Object> selectList, Object fromClause, Object whereClause) {
        this.distinct = distinct;
        this.selectList = selectList;
        this.fromClause = fromClause;
        this.whereClause = whereClause;
    }

    public boolean isDistinct() {
        return distinct;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
