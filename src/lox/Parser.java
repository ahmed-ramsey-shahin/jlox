package lox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static lox.TokenType.*;

public class Parser {

    private final List<Token> tokens;
    private int current = 0;

    private static class ParseError extends RuntimeException {}

    public Parser(List<Token> tokens) {

        this.tokens = tokens;

    }

    private boolean isAtEnd() {

        return peek().type == EOF;

    }

    private Token peek() {

        return tokens.get(current);

    }

    private Token previous() {

        return tokens.get(current - 1);

    }

    private boolean check(TokenType type) {

        if(isAtEnd()) return false;
        return peek().type == type;

    }

    private Token advance() {

        if(!isAtEnd()) current++;
        return previous();

    }

    private boolean match(TokenType... types) {

        for(TokenType type : types) {

            if(check(type)) {

                advance();
                return true;

            }

        }

        return false;

    }

    private Token consume(TokenType type, String message) {

        if(check(type)) return advance();
        throw error(peek(), message);

    }

    private ParseError error(Token token, String message) {

        Lox.error(token, message);
        return new ParseError();

    }

    private void synchronize() {

        advance();

        while (!isAtEnd()) {

            if(previous().type == SEMICOLON) return;

            switch (peek().type) {

                case CLASS, FUN, VAR, IF, WHILE, PRINT, FOR, RETURN -> {

                    return;

                }

            }

            advance();

        }

    }

    List<Stmt> parse() {

        List<Stmt> statements = new ArrayList<>();

        while (!isAtEnd()) {

            statements.add(declaration());

        }

        return statements;

    }

    private Stmt declaration() {

        try {

            if(match(VAR)) return varDeclaration();
            if(match(LEFT_BRACE)) return new Stmt.Block(block());
            return statement();

        } catch (ParseError error) {

            synchronize();
            return null;

        }

    }

    private List<Stmt> block() {

        List<Stmt> statements = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd())
            statements.add(declaration());
        consume(RIGHT_BRACE, "Expected '}' after a block.");
        return statements;

    }

    private Stmt varDeclaration() {

        Token name = consume(
                IDENTIFIER, "Expected variable name.(بزمتك يا شيخ هعمل فاريبول من غير اسم كيف يعني؟)"
        );
        Expr initializer = null;
        if(match(EQUAL))
            initializer = expression();
        consume(SEMICOLON, "Expected a ';' after variable declaration.(تاني ؟ تاني؟)");
        return new Stmt.Var(name, initializer);

    }

    private Stmt statement() {

        if(match(IF)) return ifStatement();
        if(match(WHILE)) return whileStatement();
        if(match(FOR)) return forStatement();
        if(match(PRINT)) return printStatement();
        return expressionStatement();

    }

    private Stmt forStatement() {

        consume(LEFT_PAREN, "Expected '(' after 'for'.");

        Stmt initializer;
        if(match(SEMICOLON))
            initializer = null;
        else if (match(VAR))
            initializer = varDeclaration();
        else
            initializer = expressionStatement();

        Expr condition = null;
        if(!check(SEMICOLON))
            condition = expression();
        consume(SEMICOLON, "Expected ';' after loop condition.");

        Expr increment = null;
        if(!check(RIGHT_PAREN))
            increment = expression();
        consume(RIGHT_PAREN, "Expected ')' after for clauses");

        Stmt body = declaration();

        if(increment != null) {

            body = new Stmt.Block(Arrays.asList(
                    body,
                    new Stmt.Expression(increment)
            ));

        }

        if(condition == null) condition = new Expr.Literal(true);
        body = new Stmt.While(condition, body);

        if(initializer != null)
            body = new Stmt.Block(Arrays.asList(initializer, body));

        return body;

    }

    private Stmt whileStatement() {

        consume(LEFT_PAREN, "Expected '(' after while");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expected ')' after while's condition");
        Stmt body = declaration();
        return new Stmt.While(condition, body);

    }

    private Stmt ifStatement() {

        consume(LEFT_PAREN, "Expected '(' after if");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expected ')' after if condition");

        Stmt thenBranch = declaration();
        Stmt elseBranch = null;
        if(match(ELSE))
            elseBranch = declaration();
        return new Stmt.If(condition, thenBranch, elseBranch);

    }

    private Stmt printStatement() {

        Expr value = expression();
        consume(SEMICOLON, "Expected ';' after value.(هو لسه في حد بيعمل الغلطات دي يا منوب ؟)");
        return new Stmt.Print(value);

    }

    private Stmt expressionStatement() {

        Expr expr = expression();
        consume(SEMICOLON, "Expected ';' after expression.(هو لسه في حد بيعمل الغلطات دي يا نوب ؟)");
        return new Stmt.Expression(expr);

    }

    private Expr expression() {

        return assignment();

    }

    private Expr assignment() {

        Expr expr = or();

        if(match(EQUAL)) {

            Token equals = previous();
            Expr value = assignment();

            if(expr instanceof Expr.Variable) {

                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);

            }

            throw error(equals, "Invalid assignment target.");

        }

        return expr;

    }

    private Expr or() {

        Expr expr = and();

        while (match(OR)) {

            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);

        }

        return expr;

    }

    private Expr and() {

        Expr expr = equality();

        while (match(AND)) {

            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);

        }

        return expr;

    }

    private Expr equality() {

        Expr expr = comparison();

        while(match(BANG_EQUAL, EQUAL_EQUAL)) {

            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);

        }

        return expr;

    }

    private Expr comparison() {

        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {

            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);

        }

        return expr;

    }

    private Expr term() {

        Expr expr = factor();

        while(match(MINUS, PLUS)) {

            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);

        }

        return expr;

    }

    private Expr factor() {

        Expr expr = unary();

        while(match(SLASH, STAR)) {

            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);

        }

        return expr;

    }

    private Expr unary() {

        if(match(BANG, MINUS)) {

            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);

        }

        return call();

    }

    private Expr call() {

        Expr expr = primary();

        while (true) {

            if(match(LEFT_PAREN))
                expr = finishCall(expr);
            else
                break;

        }

        return expr;

    }

    private Expr finishCall(Expr callee) {

        List<Expr> arguments = new ArrayList<>();

        if(!check(RIGHT_PAREN)) {

            do {

                if(arguments.size() >= 255)
                    error(peek(), "Can't have more than 255 arguments.");
                arguments.add(expression());

            } while (match(COMMA));

        }

        Token paren = consume(RIGHT_PAREN, "Expected ')' after arguments");
        return new Expr.Call(callee, paren, arguments);

    }

    private Expr primary() {

        if(match(FALSE)) return new Expr.Literal(false);
        if(match(TRUE)) return new Expr.Literal(true);
        if(match(NIL)) return new Expr.Literal(null);
        if(match(NUMBER, STRING)) return new Expr.Literal(previous().literal);
        if(match(IDENTIFIER)) return new Expr.Variable(previous());
        if(match(LEFT_PAREN)) {

            Expr expr = expression();
            consume(RIGHT_PAREN, "Expected ')' after expression.");
            return new Expr.Grouping(expr);

        }

        throw error(peek(), "Expected expression.");

    }

}
