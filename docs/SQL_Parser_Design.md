# NovaDB Engine: SQL Parser Design Document

## 1. Introduction

This document outlines the design for the SQL Parser component of the NovaDB Engine. The parser is a critical front-end component responsible for translating raw SQL queries into a structured, machine-readable format known as an Abstract Syntax Tree (AST). This AST then serves as the input for subsequent database components, such as the query optimizer and executor. Our design prioritizes robustness, extensibility, and performance, leveraging a hand-written recursive-descent parsing approach in Java.

## 2. SQL Grammar

The SQL grammar defines the set of valid SQL statements that NovaDB Engine will recognize and process. We will adopt a subset of standard SQL (SQL:2016) to ensure broad compatibility while focusing on core functionalities essential for a modern database. The grammar will be defined using an Extended Backus-Naur Form (EBNF) notation. This initial grammar will be expanded in future phases to support more complex SQL constructs.

### 2.1. Core Grammar Rules (EBNF)

```ebnf
<sql_statement> ::= <data_manipulation_statement>
                  | <data_definition_statement>

<data_manipulation_statement> ::= <select_statement>
                                | <insert_statement>
                                | <update_statement>
                                | <delete_statement>

<data_definition_statement> ::= <create_table_statement>
                              | <drop_table_statement>

<select_statement> ::= SELECT [ DISTINCT ] <select_list>
                     FROM <table_expression>
                     [ WHERE <search_condition> ]
                     [ ORDER BY <sort_specification_list> ]
                     [ LIMIT <integer_literal> [ OFFSET <integer_literal> ] ]

<select_list> ::= '*' | <value_expression_list>

<value_expression_list> ::= <value_expression> { ',' <value_expression> }

<value_expression> ::= <column_name>
                     | <literal>
                     | <function_call>
                     | <value_expression> <operator> <value_expression>
                     | '(' <value_expression> ')'

<table_expression> ::= <table_name>
                     | <table_name> AS <alias>

<search_condition> ::= <boolean_term> { OR <boolean_term> }

<boolean_term> ::= <boolean_factor> { AND <boolean_factor> }

<boolean_factor> ::= [ NOT ] <boolean_test>

<boolean_test> ::= <predicate> | '(' <search_condition> ')'

<predicate> ::= <value_expression> <comparison_operator> <value_expression>
              | <value_expression> IS [ NOT ] NULL
              | <value_expression> LIKE <pattern>
              | <value_expression> IN '(' <value_expression_list> ')'

<comparison_operator> ::= '=' | '<>' | '<' | '>' | '<=' | '>='

<insert_statement> ::= INSERT INTO <table_name> [ '(' <column_name_list> ')' ]
                     VALUES '(' <value_list> ')'

<column_name_list> ::= <column_name> { ',' <column_name> }

<value_list> ::= <literal> { ',' <literal> }

<update_statement> ::= UPDATE <table_name>
                     SET <set_clause_list>
                     [ WHERE <search_condition> ]

<set_clause_list> ::= <set_clause> { ',' <set_clause> }

<set_clause> ::= <column_name> '=' <value_expression>

<delete_statement> ::= DELETE FROM <table_name>
                     [ WHERE <search_condition> ]

<create_table_statement> ::= CREATE TABLE <table_name>
                           '(' <column_definition_list> ')'

<column_definition_list> ::= <column_definition> { ',' <column_definition> }

<column_definition> ::= <column_name> <data_type> [ <column_constraint> ]

<data_type> ::= INT | VARCHAR '(' <integer_literal> ')' | BOOLEAN | DOUBLE

<column_constraint> ::= PRIMARY KEY | NOT NULL | UNIQUE

<drop_table_statement> ::= DROP TABLE <table_name>

<sort_specification_list> ::= <sort_specification> { ',' <sort_specification> }

<sort_specification> ::= <column_name> [ ASC | DESC ]

<column_name> ::= <identifier>
<table_name> ::= <identifier>
<alias> ::= <identifier>
<function_call> ::= <identifier> '(' [ <value_expression_list> ] ')'
<pattern> ::= <string_literal>

<identifier> ::= <letter> { <letter> | <digit> | '_' }
<literal> ::= <string_literal> | <integer_literal> | <decimal_literal> | <boolean_literal>
<string_literal> ::= ''' { <any_character_except_single_quote> | '''' } '''
<integer_literal> ::= [ '+' | '-' ] <digit> { <digit> }
<decimal_literal> ::= [ '+' | '-' ] <digit> { <digit> } '.' <digit> { <digit> }
<boolean_literal> ::= TRUE | FALSE

<letter> ::= 'A' | ... | 'Z' | 'a' | ... | 'z'
<digit> ::= '0' | ... | '9'
```

## 3. Supported SQL Commands

Based on the initial grammar, NovaDB Engine will support the following core SQL commands:

### 3.1. Data Manipulation Language (DML)

*   **`SELECT`**: Retrieve data from one or more tables.
    *   Basic selection (`SELECT * FROM table`)
    *   Column projection (`SELECT col1, col2 FROM table`)
    *   Filtering with `WHERE` clause (equality, comparison, `AND`, `OR`, `NOT`, `IS NULL`, `LIKE`, `IN`)
    *   Ordering results with `ORDER BY` (ASC/DESC)
    *   Limiting results with `LIMIT` and `OFFSET`
    *   Distinct values (`SELECT DISTINCT col FROM table`)
    *   Simple aggregate functions (e.g., `COUNT`, `SUM`, `AVG`, `MIN`, `MAX` - *to be added in future grammar extensions*)
*   **`INSERT`**: Add new rows to a table.
    *   `INSERT INTO table (col1, col2) VALUES (val1, val2)`
    *   `INSERT INTO table VALUES (val1, val2, ...)`
*   **`UPDATE`**: Modify existing rows in a table.
    *   `UPDATE table SET col1 = val1 WHERE condition`
*   **`DELETE`**: Remove rows from a table.
    *   `DELETE FROM table WHERE condition`

### 3.2. Data Definition Language (DDL)

*   **`CREATE TABLE`**: Define a new table schema.
    *   Column definitions with data types (`INT`, `VARCHAR(n)`, `BOOLEAN`, `DOUBLE`)
    *   Column constraints (`PRIMARY KEY`, `NOT NULL`, `UNIQUE`)
*   **`DROP TABLE`**: Remove an existing table.

### 3.3. Future Extensions

Future phases will expand the grammar and supported commands to include:

*   Joins (`INNER JOIN`, `LEFT JOIN`, etc.)
*   Subqueries
*   Aggregate functions and `GROUP BY`, `HAVING` clauses
*   `ALTER TABLE` statements
*   `CREATE INDEX`, `DROP INDEX`
*   Views, Stored Procedures, Triggers
*   Transactions (`BEGIN`, `COMMIT`, `ROLLBACK`)
*   User and permission management
*   Advanced data types and functions

## 4. Tokenizer (Lexer) Architecture

The tokenizer, often referred to as the lexer or scanner, is the first stage of the parsing process. Its responsibility is to read the raw SQL input string and break it down into a stream of meaningful units called **tokens**. Each token represents a fundamental building block of the SQL language, such as keywords, identifiers, operators, literals, and punctuation.

### 4.1. Design Decisions

*   **Hand-written Lexer**: We will implement a hand-written lexer rather than using a lexer generator (e.g., JFlex, ANTLR). This approach offers several advantages:
    *   **Fine-grained Control**: Allows for precise control over tokenization rules, especially for SQL-specific nuances like quoted identifiers or string literals with escaped characters.
    *   **Performance**: Hand-written lexers can often be optimized for performance, avoiding the overhead of generated code.
    *   **Reduced Dependencies**: Minimizes external library dependencies, keeping the core database engine lean.
    *   **Easier Debugging**: Debugging a hand-written lexer is generally more straightforward than debugging generated code.
*   **State Machine Approach**: The lexer will operate as a finite state machine, transitioning between states based on the input characters. This is a natural fit for recognizing different token types.
*   **Lookahead**: The lexer will support a single-character lookahead to distinguish between similar tokens (e.g., `=` vs. `==` or `<` vs. `<>`).
*   **Error Reporting**: The lexer will detect and report lexical errors (e.g., unclosed strings, invalid characters) with precise line and column information.

### 4.2. Key Components

*   `Token`: A simple data class representing a recognized token. It will contain:
    *   `TokenType`: An enum (e.g., `KEYWORD_SELECT`, `IDENTIFIER`, `LITERAL_STRING`, `OPERATOR_EQ`, `COMMA`).
    *   `lexeme`: The actual string value of the token (e.g., "SELECT", "my_table", "'hello'").
    *   `line`: The line number where the token starts.
    *   `column`: The column number where the token starts.
*   `TokenType`: An enum defining all possible token types recognized by the lexer. This will include SQL keywords, operators, punctuation, literals, and identifiers.
*   `Lexer`: The main lexer class.
    *   `Lexer(String sqlInput)`: Constructor taking the SQL query string.
    *   `nextToken()`: Returns the next `Token` from the input stream. Skips whitespace and comments.
    *   `peekToken()`: Returns the next `Token` without consuming it (for lookahead).
    *   `addKeyword(String keyword, TokenType type)`: Method to register SQL keywords and their corresponding token types.

### 4.3. Lexer Workflow

1.  The `Lexer` is initialized with the SQL input string.
2.  When `nextToken()` is called, it iterates through the input character by character.
3.  It identifies the longest possible match for a token based on predefined rules (e.g., keywords, identifiers, numbers, strings, operators).
4.  Whitespace and comments (`--` for single-line, `/* */` for multi-line) are skipped.
5.  Once a token is recognized, a `Token` object is created with its type, lexeme, and position, and returned.
6.  If an unexpected character sequence is encountered, a `LexicalException` is thrown.

## 5. Parser Architecture

The parser takes the stream of tokens generated by the lexer and verifies that they conform to the defined SQL grammar. If the tokens form a syntactically correct SQL statement, the parser constructs an Abstract Syntax Tree (AST) representing the query's structure. If not, it reports a syntax error.

### 5.1. Design Decisions

*   **Hand-written Recursive-Descent Parser**: Similar to the lexer, we will implement a hand-written recursive-descent parser. This parsing strategy is well-suited for our EBNF grammar and offers:
    *   **Simplicity and Readability**: The parser structure directly mirrors the grammar rules, making it intuitive to understand and maintain.
    *   **Direct AST Construction**: AST nodes can be constructed directly within the parsing methods, simplifying the process.
    *   **Good Error Reporting**: It's relatively easy to provide meaningful error messages with context (expected vs. found tokens).
    *   **No External Tools**: Avoids the need for complex parser generators, reducing build complexity and external dependencies.
*   **Predictive Parsing**: The parser will be predictive, meaning it uses lookahead (via `peekToken()` from the lexer) to decide which grammar rule to apply next without backtracking. This requires an LL(1) or similar grammar.
*   **Modular Design**: Each major grammar rule (e.g., `parseSelectStatement()`, `parseCreateTableStatement()`) will correspond to a dedicated parsing method. This promotes modularity and makes the parser easier to extend.

### 5.2. Key Components

*   `Parser`: The main parser class.
    *   `Parser(Lexer lexer)`: Constructor taking a `Lexer` instance.
    *   `parse()`: The entry point for parsing, which dispatches to specific statement parsing methods (e.g., `parseStatement()`). Returns an `ASTNode` (the root of the AST).
    *   `consume(TokenType expectedType)`: Consumes the current token if it matches `expectedType`, otherwise throws a `SyntaxException`.
    *   `match(TokenType expectedType)`: Checks if the current token matches `expectedType` without consuming it.
    *   `parseSelectStatement()`: Parses a `SELECT` statement and returns a `SelectStatement` AST node.
    *   `parseInsertStatement()`: Parses an `INSERT` statement and returns an `InsertStatement` AST node.
    *   ... (similar methods for other SQL statements and expressions)
*   `ASTNode`: The base interface or abstract class for all nodes in the Abstract Syntax Tree.

### 5.3. Parser Workflow

1.  The `Parser` is initialized with a `Lexer` instance.
2.  The `parse()` method is called to begin parsing the entire SQL query.
3.  It calls `nextToken()` on the `Lexer` to get the first token.
4.  Based on the current token's `TokenType`, it dispatches to the appropriate parsing method (e.g., if `SELECT` keyword, call `parseSelectStatement()`).
5.  Each parsing method recursively calls other parsing methods to match sub-rules of the grammar.
6.  During parsing, `consume()` is used to advance the token stream and verify expected tokens.
7.  As grammar rules are successfully matched, corresponding `ASTNode` objects are instantiated and linked together to form the AST.
8.  If `consume()` fails to match an expected token, a `SyntaxException` is thrown, halting the parsing process and reporting the error.
9.  Upon successful parsing, the root `ASTNode` of the complete AST is returned.

## 6. Parser Workflow Diagram

```mermaid
graph TD
    A[SQL Query String] --> B(Lexer)
    B --> C{Token Stream}
    C --> D(Parser)
    D --> E[Abstract Syntax Tree (AST)]
    B -- Lexical Errors --> F[Error Handler]
    D -- Syntax Errors --> F
    E --> G[Query Optimizer / Executor]
```

**Explanation:**

*   The **SQL Query String** is the raw input.
*   The **Lexer** converts the string into a **Token Stream**, skipping whitespace and comments. It reports **Lexical Errors**.
*   The **Parser** consumes the **Token Stream** and, if valid, constructs the **Abstract Syntax Tree (AST)**. It reports **Syntax Errors**.
*   The **AST** is then passed to subsequent components like the **Query Optimizer / Executor** for further processing.
*   The **Error Handler** collects and reports all parsing-related errors.

## 7. Abstract Syntax Tree (AST)

The Abstract Syntax Tree (AST) is an intermediate representation of the parsed SQL query. It is a tree structure where each node represents a construct in the source code, such as a statement, expression, or declaration. The AST abstracts away the syntactic details of the SQL grammar, focusing on the logical structure and meaning of the query. This structured representation is then used by subsequent phases of the query processor, including semantic analysis, optimization, and execution.

### 7.1. Design Decisions

*   **Hierarchical Structure**: The AST will be a hierarchical tree, with the root representing the entire SQL statement and child nodes representing its components (e.g., clauses, expressions, literals).
*   **Type-Safe Nodes**: Each type of SQL construct will have a corresponding AST node class. This provides type safety and allows for specific processing logic based on the node type.
*   **Visitor Pattern**: To facilitate traversal and processing of the AST by different components (e.g., semantic analyzer, optimizer, code generator), we will implement the Visitor pattern. This decouples the AST structure from the operations performed on it, making the system more extensible.
*   **Immutability**: AST nodes, once constructed, should ideally be immutable to simplify concurrency and prevent accidental modification during later processing phases.

### 7.2. Key AST Node Types

All AST nodes will implement a common `ASTNode` interface or extend an abstract `AbstractASTNode` class, providing methods like `accept(ASTVisitor visitor)`.

*   **`Statement` (Abstract Base Class)**:
    *   `SelectStatement`: Represents a `SELECT` query.
        *   `boolean distinct`
        *   `List<Expression> selectList`
        *   `TableExpression fromClause`
        *   `Expression whereClause`
        *   `List<SortSpecification> orderByClause`
        *   `LimitClause limitClause`
    *   `InsertStatement`: Represents an `INSERT` query.
        *   `Identifier tableName`
        *   `List<Identifier> columnNames`
        *   `List<Expression> values`
    *   `UpdateStatement`: Represents an `UPDATE` query.
        *   `Identifier tableName`
        *   `List<SetClause> setClauses`
        *   `Expression whereClause`
    *   `DeleteStatement`: Represents a `DELETE` query.
        *   `Identifier tableName`
        *   `Expression whereClause`
    *   `CreateTableStatement`: Represents a `CREATE TABLE` query.
        *   `Identifier tableName`
        *   `List<ColumnDefinition> columnDefinitions`
    *   `DropTableStatement`: Represents a `DROP TABLE` query.
        *   `Identifier tableName`

*   **`Expression` (Abstract Base Class)**:
    *   `LiteralExpression`: Represents a constant value (e.g., `10`, `'hello'`, `TRUE`).
        *   `Object value`
        *   `DataType type`
    *   `ColumnRefExpression`: Represents a reference to a column.
        *   `Identifier columnName`
        *   `Identifier tableName` (optional)
    *   `BinaryExpression`: Represents an expression with two operands and an operator (e.g., `a + b`, `x = y`).
        *   `Expression left`
        *   `Operator operator`
        *   `Expression right`
    *   `UnaryExpression`: Represents an expression with one operand and an operator (e.g., `NOT condition`, `-value`).
        *   `Operator operator`
        *   `Expression operand`
    *   `FunctionCallExpression`: Represents a function invocation (e.g., `COUNT(*)`, `SUM(col)`).
        *   `Identifier functionName`
        *   `List<Expression> arguments`
    *   `PredicateExpression` (Abstract Base Class for WHERE clause conditions):
        *   `ComparisonPredicate`: `col = val`
        *   `IsNullPredicate`: `col IS NULL`
        *   `LikePredicate`: `col LIKE pattern`
        *   `InPredicate`: `col IN (val1, val2)`

*   **`TableExpression` (Abstract Base Class)**:
    *   `TableRef`: Reference to a base table.
        *   `Identifier tableName`
        *   `Identifier alias` (optional)

*   **`ColumnDefinition`**: Represents a column definition in `CREATE TABLE`.
    *   `Identifier columnName`
    *   `DataType dataType`
    *   `List<ColumnConstraint> constraints`

*   **`SetClause`**: Represents a `SET` assignment in `UPDATE`.
    *   `Identifier columnName`
    *   `Expression valueExpression`

*   **`SortSpecification`**: Represents an `ORDER BY` item.
    *   `Expression sortKey`
    *   `SortOrder order` (ASC/DESC)

*   **`LimitClause`**: Represents `LIMIT` and `OFFSET`.
    *   `int limit`
    *   `int offset`

### 7.3. Example AST for `SELECT col1 FROM my_table WHERE id = 10`

```
SelectStatement
├── SelectList
│   └── ColumnRefExpression (col1)
├── FromClause
│   └── TableRef (my_table)
└── WhereClause
    └── BinaryExpression (=)
        ├── ColumnRefExpression (id)
        └── LiteralExpression (10)
```

## 8. Error Handling

Robust error handling is crucial for providing a good user experience and aiding in debugging. The parser will provide clear, actionable error messages with precise location information.

### 8.1. Custom Exception Classes

We will define specific exception classes to differentiate between lexical and syntactic errors:

*   `LexicalException`: Thrown by the `Lexer` when an invalid character sequence is encountered or a token cannot be formed.
    *   Contains `message`, `line`, `column`.
*   `SyntaxException`: Thrown by the `Parser` when the token stream does not conform to the SQL grammar.
    *   Contains `message`, `line`, `column`, `expectedTokenTypes`, `foundTokenType`.

### 8.2. Error Reporting

When an error occurs, the exception will be caught by the top-level parsing method, and a detailed error message will be constructed. This message will include:

*   The type of error (lexical or syntax).
*   A descriptive message explaining what went wrong.
*   The line and column number where the error occurred.
*   For `SyntaxException`, it will also indicate what token types were expected versus what was actually found.
*   A snippet of the SQL query around the error location to provide context.

### 8.3. Error Recovery (Initial Approach)

For the initial version, the parser will adopt a simple error recovery strategy: **halt on first error**. When a `LexicalException` or `SyntaxException` is thrown, the parsing process will stop, and the error will be reported. This approach simplifies the parser implementation and is generally acceptable for a database system where SQL queries are expected to be syntactically correct. Future enhancements might include more sophisticated error recovery mechanisms (e.g., panic mode, error productions) to attempt to parse beyond a single error, but this adds significant complexity and is not a priority for the initial release.

### 8.4. Example Error Messages

*   **Lexical Error**: `Lexical Error at line 1, column 7: Unexpected character '$'.` (For `SELECT $ FROM table;`)
*   **Syntax Error**: `Syntax Error at line 1, column 14: Expected 'FROM' but found 'WHERE'.` (For `SELECT col1 WHERE id = 10;`)
*   **Syntax Error**: `Syntax Error at line 1, column 20: Expected identifier or literal but found ')'.` (For `INSERT INTO my_table VALUES (10, );`)

## 9. Java Package Structure

The parser module will reside within the `com.novadb.query.parser` package, with sub-packages for lexer, ast, and exceptions. This structure promotes modularity and clear separation of concerns within the query processing layer.

```
src/main/java/com/novadb/query/parser/
├── Lexer.java                  # Main Lexer class
├── Token.java                  # Token data class
├── TokenType.java              # Enum for all token types
├── Parser.java                 # Main Parser class
├── ast/                        # Abstract Syntax Tree (AST) node definitions
│   ├── ASTNode.java            # Base interface/abstract class for all AST nodes
│   ├── Statement.java          # Abstract base for all SQL statements
│   │   ├── SelectStatement.java
│   │   ├── InsertStatement.java
│   │   ├── UpdateStatement.java
│   │   ├── DeleteStatement.java
│   │   ├── CreateTableStatement.java
│   │   └── DropTableStatement.java
│   ├── Expression.java         # Abstract base for all expressions
│   │   ├── LiteralExpression.java
│   │   ├── ColumnRefExpression.java
│   │   ├── BinaryExpression.java
│   │   ├── UnaryExpression.java
│   │   ├── FunctionCallExpression.java
│   │   └── PredicateExpression.java
│   ├── TableExpression.java    # Abstract base for table references
│   │   └── TableRef.java
│   ├── ColumnDefinition.java
│   ├── SetClause.java
│   ├── SortSpecification.java
│   ├── LimitClause.java
│   ├── DataType.java           # Enum for supported data types
│   └── Operator.java           # Enum for supported operators
├── exceptions/                 # Custom exception classes
│   ├── LexicalException.java
│   └── SyntaxException.java
└── ASTVisitor.java             # Interface for AST traversal (Visitor pattern)
```

**Explanation of Structure:**

*   **`com.novadb.query.parser`**: The root package for the SQL parser component.
*   **`Lexer.java`, `Token.java`, `TokenType.java`**: Core components of the lexical analysis phase.
*   **`Parser.java`**: The main class responsible for syntactic analysis and AST construction.
*   **`ast/`**: A dedicated sub-package for all Abstract Syntax Tree node definitions. This keeps the AST structure organized and separate from the parsing logic.
    *   Hierarchical organization within `ast/` (e.g., `Statement`, `Expression`) reflects the grammar structure and promotes a clear object model.
*   **`exceptions/`**: Contains custom exception classes for specific error conditions encountered during lexical and syntactic analysis.
*   **`ASTVisitor.java`**: Defines the interface for the Visitor pattern, enabling decoupled operations on the AST.

### 10.1. Text-based UML Class Diagram

```text
+-----------------+
|      Lexer      |
+-----------------+
| - sqlInput: String  |
| - position: int     |
| - currentChar: char |
+-----------------+
| + nextToken(): Token |
| + peekToken(): Token |
| - advance(): void   |
| - skipWhitespace(): void |
| - readIdentifier(): String |
| - readNumber(): String |
| - readString(): String |
+-----------------+
         | uses
         V
+-----------------+
|      Token      |
+-----------------+
| + type: TokenType   |
| + lexeme: String    |
| + line: int         |
| + column: int       |
+-----------------+
         ^
         |
+-----------------+
|    TokenType    |
+-----------------+
| KEYWORD_SELECT  |
| IDENTIFIER      |
| LITERAL_STRING  |
| OPERATOR_EQ     |
| ...             |
+-----------------+

+-----------------+
|      Parser     |
+-----------------+
| - lexer: Lexer      |
| - currentToken: Token |
+-----------------+
| + parse(): ASTNode  |
| - consume(expected: TokenType): void |
| - match(expected: TokenType): boolean |
| - parseStatement(): Statement |
| - parseSelectStatement(): SelectStatement |
| - parseExpression(): Expression |
| ...             |
+-----------------+
         | creates
         V
+-----------------+
|     ASTNode     |
+-----------------+
| + accept(visitor: ASTVisitor): void |
+-----------------+
         ^
         | implements
+-----------------+
|   ASTVisitor    |
+-----------------+
| + visit(node: SelectStatement): void |
| + visit(node: InsertStatement): void |
| ...             |
+-----------------+

+-----------------+
|    Statement    |
+-----------------+
| <<abstract>>    |
+-----------------+
         ^
         | extends
+-----------------+
| SelectStatement |
+-----------------+
| + distinct: boolean |
| + selectList: List<Expression> |
| + fromClause: TableExpression |
| + whereClause: Expression |
| + orderByClause: List<SortSpecification> |
| + limitClause: LimitClause |
+-----------------+

+-----------------+
| InsertStatement |
+-----------------+
| + tableName: Identifier |
| + columnNames: List<Identifier> |
| + values: List<Expression> |
+-----------------+

+-----------------+
| UpdateStatement |
+-----------------+
| + tableName: Identifier |
| + setClauses: List<SetClause> |
| + whereClause: Expression |
+-----------------+

+-----------------+
| DeleteStatement |
+-----------------+
| + tableName: Identifier |
| + whereClause: Expression |
+-----------------+

+-----------------+
| CreateTableStatement |
+-----------------+
| + tableName: Identifier |
| + columnDefinitions: List<ColumnDefinition> |
+-----------------+

+-----------------+
| DropTableStatement |
+-----------------+
| + tableName: Identifier |
+-----------------+

+-----------------+
|    Expression   |
+-----------------+
| <<abstract>>    |
+-----------------+
         ^
         | extends
+-----------------+
| LiteralExpression |
+-----------------+
| + value: Object   |
| + type: DataType  |
+-----------------+

+-----------------+
| ColumnRefExpression |
+-----------------+
| + columnName: Identifier |
| + tableName: Identifier |
+-----------------+

+-----------------+
| BinaryExpression |
+-----------------+
| + left: Expression |
| + operator: Operator |
| + right: Expression |
+-----------------+

+-----------------+
| UnaryExpression |
+-----------------+
| + operator: Operator |
| + operand: Expression |
+-----------------+

+-----------------+
| FunctionCallExpression |
+-----------------+
| + functionName: Identifier |
| + arguments: List<Expression> |
+-----------------+

+-----------------+
| PredicateExpression |
+-----------------+
| <<abstract>>    |
+-----------------+
         ^
         | extends
+-----------------+
| ComparisonPredicate |
+-----------------+

+-----------------+
| IsNullPredicate |
+-----------------+

+-----------------+
| LikePredicate   |
+-----------------+

+-----------------+
| InPredicate     |
+-----------------+

+-----------------+
| TableExpression |
+-----------------+
| <<abstract>>    |
+-----------------+
         ^
         | extends
+-----------------+
|    TableRef     |
+-----------------+
| + tableName: Identifier |
| + alias: Identifier |
+-----------------+

+-----------------+
| ColumnDefinition |
+-----------------+
| + columnName: Identifier |
| + dataType: DataType |
| + constraints: List<ColumnConstraint> |
+-----------------+

+-----------------+
|    SetClause    |
+-----------------+
| + columnName: Identifier |
| + valueExpression: Expression |
+-----------------+

+-----------------+
| SortSpecification |
+-----------------+
| + sortKey: Expression |
| + order: SortOrder |
+-----------------+

+-----------------+
|   LimitClause   |
+-----------------+
| + limit: int      |
| + offset: int     |
+-----------------+

+-----------------+
|    DataType     |
+-----------------+
| INT             |
| VARCHAR         |
| BOOLEAN         |
| DOUBLE          |
+-----------------+

+-----------------+
|    Operator     |
+-----------------+
| EQ              |
| NEQ             |
| LT              |
| GT              |
| ...             |
+-----------------+

+-----------------+
| ColumnConstraint |
+-----------------+
| PRIMARY_KEY     |
| NOT_NULL        |
| UNIQUE          |
+-----------------+

+-----------------+
|    SortOrder    |
+-----------------+
| ASC             |
| DESC            |
+-----------------+

+-----------------+
| LexicalException |
+-----------------+
| + message: String |
| + line: int       |
| + column: int     |
+-----------------+

+-----------------+
| SyntaxException |
+-----------------+
| + message: String |
| + line: int       |
| + column: int     |
| + expectedTokenTypes: List<TokenType> |
| + foundTokenType: TokenType |
+-----------------+

Lexer "uses" Token
Parser "uses" Lexer
Parser "creates" ASTNode
ASTNode "implements" ASTVisitor

Statement "extends" ASTNode
Expression "extends" ASTNode
TableExpression "extends" ASTNode
ColumnDefinition "extends" ASTNode
SetClause "extends" ASTNode
SortSpecification "extends" ASTNode
LimitClause "extends" ASTNode

SelectStatement "extends" Statement
InsertStatement "extends" Statement
UpdateStatement "extends" Statement
DeleteStatement "extends" Statement
CreateTableStatement "extends" Statement
DropTableStatement "extends" Statement

LiteralExpression "extends" Expression
ColumnRefExpression "extends" Expression
BinaryExpression "extends" Expression
UnaryExpression "extends" Expression
FunctionCallExpression "extends" Expression
PredicateExpression "extends" Expression

ComparisonPredicate "extends" PredicateExpression
IsNullPredicate "extends" PredicateExpression
LikePredicate "extends" PredicateExpression
InPredicate "extends" PredicateExpression

TableRef "extends" TableExpression

SelectStatement "has" Expression
SelectStatement "has" TableExpression
SelectStatement "has" SortSpecification
SelectStatement "has" LimitClause

InsertStatement "has" Identifier
InsertStatement "has" Expression

UpdateStatement "has" Identifier
UpdateStatement "has" SetClause
UpdateStatement "has" Expression

DeleteStatement "has" Identifier
DeleteStatement "has" Expression

CreateTableStatement "has" Identifier
CreateTableStatement "has" ColumnDefinition

ColumnDefinition "has" Identifier
ColumnDefinition "has" DataType
ColumnDefinition "has" ColumnConstraint

SetClause "has" Identifier
SetClause "has" Expression

SortSpecification "has" Expression
SortSpecification "has" SortOrder

BinaryExpression "has" Expression
BinaryExpression "has" Operator

UnaryExpression "has" Operator
UnaryExpression "has" Expression

FunctionCallExpression "has" Identifier
FunctionCallExpression "has" Expression

Lexer "throws" LexicalException
Parser "throws" SyntaxException
```

## 11. Future Roadmap

The initial SQL Parser design provides a solid foundation for NovaDB Engine. The following roadmap outlines future enhancements and extensions:

### Phase 2: Advanced SQL Features

*   **Joins**: Implement parsing for `INNER JOIN`, `LEFT JOIN`, `RIGHT JOIN`, `FULL OUTER JOIN`.
*   **Subqueries**: Support for subqueries in `SELECT`, `FROM`, and `WHERE` clauses.
*   **Aggregate Functions**: Extend grammar and AST to support `COUNT`, `SUM`, `AVG`, `MIN`, `MAX`, and `GROUP BY`, `HAVING` clauses.
*   **Set Operations**: Add support for `UNION`, `INTERSECT`, `EXCEPT`.
*   **Window Functions**: Introduce parsing for advanced analytical functions.
*   **`ALTER TABLE`**: Implement DDL for modifying existing table schemas.
*   **`CREATE INDEX`, `DROP INDEX`**: Support for index management.

### Phase 3: Programmability and Control

*   **Stored Procedures and Functions**: Extend the parser to handle procedural SQL constructs.
*   **Triggers**: Implement DDL for creating and managing database triggers.
*   **Transactions**: Add `BEGIN`, `COMMIT`, `ROLLBACK` statements.
*   **User and Permission Management**: Parsing for `CREATE USER`, `GRANT`, `REVOKE` statements.

### Phase 4: AI Integration and Extensibility

*   **Custom Functions**: Allow users to define and register custom functions, which the parser can then recognize.
*   **Semantic Parsing Enhancements**: Integrate with the Catalog Manager to perform early semantic checks during parsing.
*   **AI-driven Query Suggestions**: Explore how the AST can be used to provide intelligent query suggestions or auto-completion.
*   **Alternative Query Languages**: Design an extensible parsing framework to potentially support other query languages (e.g., NoSQL-like queries) in the future.

## 12. Conclusion

This SQL Parser Design Document lays out a comprehensive plan for building a robust, extensible, and performant SQL parser for NovaDB Engine. By adhering to a hand-written recursive-descent approach, defining a clear grammar, structuring the AST effectively, and implementing meticulous error handling, we aim to create a core component that will serve as a reliable foundation for all subsequent query processing functionalities. The outlined roadmap ensures a phased approach to feature development, allowing for continuous improvement and adaptation to future requirements, including advanced AI integration.
