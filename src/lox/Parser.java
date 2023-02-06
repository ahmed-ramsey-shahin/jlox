package lox;

import java.util.ArrayList;
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
            return statement();

        } catch (ParseError error) {

            synchronize();
            return null;

        }

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

        if(match(PRINT)) return printStatement();
        return expressionStatement();

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

        return equality();

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

        return primary();

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
